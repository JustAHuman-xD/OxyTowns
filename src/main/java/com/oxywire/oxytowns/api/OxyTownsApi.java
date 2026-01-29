package com.oxywire.oxytowns.api;

import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.cache.TownCache;
import com.oxywire.oxytowns.config.Config;
import com.oxywire.oxytowns.config.Messages;
import com.oxywire.oxytowns.config.messaging.Message;
import com.oxywire.oxytowns.entities.impl.plot.Plot;
import com.oxywire.oxytowns.entities.impl.town.Town;
import com.oxywire.oxytowns.entities.types.Role;
import com.oxywire.oxytowns.entities.types.Upgrade;
import com.oxywire.oxytowns.entities.types.perms.Permission;
import com.oxywire.oxytowns.events.TownClaimEvent;
import com.oxywire.oxytowns.events.TownUnclaimEvent;
import com.oxywire.oxytowns.menu.town.OutpostsMenu;
import com.oxywire.oxytowns.utils.ChunkPosition;
import com.oxywire.oxytowns.utils.RegionUtils;
import com.oxywire.oxytowns.utils.TownUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public final class OxyTownsApi {

    private final TownCache townCache;

    public OxyTownsApi(final TownCache townCache) {
        this.townCache = townCache;
    }

    /**
     * @return A mutable map with the default town permissions
     */
    public Map<Role, Set<Permission>> createDefault() {
        return Arrays.stream(Role.values())
            .filter(role -> role != Role.MAYOR)
            .collect(Collectors.toMap(UnaryOperator.identity(), v -> EnumSet.noneOf(Permission.class)));
    }

    /**
     * Tries to claim a chunk for a town as a player with all necessary checks
     * This is the logic used for the town claim commands
     *
     * @param player the player
     * @param town the town
     * @param chunksToClaim the chunks to claim
     * @return true if the claim was successful, false otherwise
     */
    public boolean tryClaim(Player player, Town town, Set<ChunkPosition> chunksToClaim) {
        final Messages messages = Messages.get();
        final Config config = Config.get();

        if (!town.hasPermission(player.getUniqueId(), Permission.CLAIM_UNCLAIM)) {
            messages.getTown().getNoPermissionClaim().send(player);
            return false;
        }

        final List<String> blacklistedWorlds = config.getBlacklistedWorlds();
        final double claimPrice = config.getClaimPrice();

        if (blacklistedWorlds.contains(player.getLocation().getWorld().getName())) {
            messages.getTown().getClaim().getErrorBlacklistedWorld().send(player);
            return false;
        }

        if (this.townCache.getTownByLocation(player.getLocation()) != null) {
            messages.getTown().getClaim().getChunkAlreadyClaimed().send(player);
            return false;
        }

        if (!TownUtils.townExclusive(this.townCache, town, player, 5, true)) {
            messages.getTown().getClaim().getTownNear().send(player);
            return false;
        }

        if (!town.getClaimedChunks().isEmpty() && !TownUtils.isNextToBorder(player, town)) {
            messages.getTown().getClaim().getErrorConnectedClaims().send(player);
            return false;
        }

        if (RegionUtils.isInRegion(player.getLocation())) {
            messages.getTown().getClaim().getErrorProtectedClaim().send(player);
            return false;
        }

        if (town.getUpgradeValue(Upgrade.CLAIMS) <= town.getClaimedChunks().size()) {
            messages.getTown().getClaim().getErrorUpgradeRequired().send(player);
            return false;
        }

        int totalPriceToClaim = (int) (claimPrice * chunksToClaim.size());
        if (town.getBankValue() < totalPriceToClaim) {
            messages.getTown().getClaim().getErrorCannotAffordClaim().send(player);
            return false;
        }

        int notClaimed = 0;
        int finalPrice = 0;
        for (ChunkPosition chunkPosition : chunksToClaim) {
            final Location location = Bukkit.getWorld(chunkPosition.getWorld()).getHighestBlockAt(chunkPosition.getX() << 4, chunkPosition.getZ() << 4).getLocation();
            if (RegionUtils.isInRegion(location) || town.hasClaimed(chunkPosition)) {
                notClaimed++;
                continue;
            }

            if (!town.claimChunks(chunkPosition, player, TownClaimEvent.ClaimCause.PLAYER)) {
                notClaimed++;
                continue;
            }
            finalPrice += claimPrice;
            town.removeWorth(claimPrice);

            if (town.getClaimedChunks().isEmpty() && town.getHome() == null) {
                town.setHome(player.getLocation());
            }
        }

        if (notClaimed == chunksToClaim.size()) {
            messages.getTown().getClaim().getClaimCancelled().send(player);
            return false;
        }

        Message message = notClaimed > 0 ? messages.getTown().getClaim().getPartialClaimSuccess() : messages.getTown().getClaim().getClaimSuccess();
        message.send(
            player,
            Placeholder.unparsed("claims", String.valueOf(chunksToClaim.size() - notClaimed)),
            Placeholder.unparsed("total", String.valueOf(chunksToClaim.size())),
            Formatter.number("price", finalPrice)
        );
        return true;
    }

    /**
     * Tries to unclaim a chunk for a town as a player with all necessary checks
     * This is the logic used for the town unclaim commands
     *
     * @param player the player
     * @param town the town
     * @param outpostMenu whether to open the outpost menu on success
     * @return true if the unclaim was successful, false otherwise
     */
    public boolean tryUnclaim(Player player, Town town, boolean outpostMenu) {
        final Messages messages = Messages.get();
        final ChunkPosition chunkPosition = ChunkPosition.chunkPosition(player.getLocation().getChunk());

        if (!town.hasPermission(player.getUniqueId(), Permission.CLAIM_UNCLAIM)) {
            messages.getTown().getNoPermissionClaim().send(player);
            return false;
        }

        if (!town.hasClaimed(chunkPosition)) {
            messages.getTown().getUnclaim().getNotClaimed().send(player);
            return false;
        }

        if (!TownUtils.townExclusive(this.townCache, town, player, 1, false)) {
            messages.getTown().getUnclaim().getChunkLinkedOutpost().send(player);
            return false;
        }

        boolean outpost = town.hasOutpost(chunkPosition);
        if (!town.unclaimChunk(chunkPosition, player, TownUnclaimEvent.UnclaimCause.PLAYER)) {
            messages.getTown().getUnclaim().getUnclaimCancelled().send(player);
            return false;
        }

        if (outpost) {
            final Config config = Config.get();
            final double outpostRefund = config.getOutpostRefund();
            messages.getTown().getUnclaim().getOutpostUnclaimSuccess().send(player,
                Formatter.number("refund", outpostRefund));
            if (outpostMenu) {
                OutpostsMenu.open(player, town);
            }
        } else {
            messages.getTown().getUnclaim().getUnclaimSuccess().send(player);
        }
        return true;
    }

    /**
     * Checks if a player has permission to do something at a given location
     *
     * @param player player
     * @param permission the permission
     * @param location the location
     * @return true if the player has permission, false otherwise
     */
    public boolean hasPermission(final Player player, final Permission permission, final Location location) {
        return hasPermission(player, permission, ChunkPosition.chunkPosition(location));
    }

    /**
     * Checks if a player has permission to do something at a given chunk position
     *
     * @param player the player
     * @param permission the permission
     * @param position the position
     * @return true if the player has permission, false otherwise
     */
    public boolean hasPermission(final Player player, final Permission permission, final ChunkPosition position) {
        return hasPermission(player.getUniqueId(), permission, position);
    }

    /**
     * Checks if a player has permission to do something at a given chunk position
     *
     * @param player the player's uuid
     * @param permission the permission
     * @param chunkPosition the position
     * @return true if the player has permission, false otherwise
     */
    public boolean hasPermission(final UUID player, final Permission permission, final ChunkPosition chunkPosition) {
        return hasPermission(player, permission, chunkPosition, null);
    }

    /**
     * Checks if a player has permission to do something at a given chunk position
     *
     * @param player the player's uuid
     * @param permission the permission
     * @param chunkPosition the position
     * @param queryObject the object to check
     * @return true if the player has permission, false otherwise
     */
    public boolean hasPermission(final UUID player, final Permission permission, final ChunkPosition chunkPosition, @Nullable final Object queryObject) {
        final TownCache townCache = OxyTownsPlugin.get().getTownCache();

        // They're bypassing via /ta bypass
        if (townCache.isBypassing(player)) {
            return true;
        }

        final Town town = OxyTownsPlugin.get().getTownCache().getTownByChunk(chunkPosition);
        // It's wilderness
        if (town == null) {
            return true;
        }

        // Always allow if the player is the mayor or has plots_modify
        if (town.hasPermission(player, Permission.PLOTS_MODIFY)) {
            return true;
        }

        // Get the plot
        final Plot foundPlot = town.getPlot(chunkPosition);
        // If there isn't a plot there, check the town perms
        if (foundPlot == null || !foundPlot.isModified()) {
            return town.hasPermission(player, permission);
        }

        // There is a plot, allow if the plot type override supports it
        if (foundPlot.getType().test(queryObject)) return true;

        // Always allow plot members
        return foundPlot.getAssignedMembers().contains(player);
    }
}
