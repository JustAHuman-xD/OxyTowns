package com.oxywire.oxytowns.command.commands.town.sub;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.cache.TownCache;
import com.oxywire.oxytowns.command.annotation.MustBeInTown;
import com.oxywire.oxytowns.command.annotation.SendersTown;
import com.oxywire.oxytowns.config.Config;
import com.oxywire.oxytowns.config.Messages;
import com.oxywire.oxytowns.entities.impl.town.Town;
import com.oxywire.oxytowns.utils.ChunkPosition;
import com.oxywire.oxytowns.utils.TownUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import org.bukkit.entity.Player;

import java.util.Set;

public final class RadiusClaimCommand {

    private final OxyTownsPlugin plugin;

    public RadiusClaimCommand(final OxyTownsPlugin plugin) {
        this.plugin = plugin;
    }

    @CommandMethod("town|t claim <radius>")
    @CommandDescription("Claim a radius for your town")
    @MustBeInTown
    public void onClaim(final Player sender, final @SendersTown Town town, final @Argument("radius") int radius) {
        final Messages messages = Messages.get();
        final Config config = Config.get();

        if (radius == 0) {
            messages.getTown().getClaim().getErrorZeroInput().send(sender);
            return;
        }

        if (radius < 0) {
            messages.getTown().getClaim().getErrorNegativeInput().send(sender);
            return;
        }

        int maximumRadius = config.getMaxClaimRadius();

        if (radius > maximumRadius) {
            messages.getTown().getClaim().getErrorOverMaxRadius().send(sender, Formatter.number("max", maximumRadius));
            return;
        }

        final Set<ChunkPosition> chunksToClaim = TownUtils.getChunksAroundPlayer(sender, radius);
        plugin.getOxyTownsApi().tryClaim(sender, town, chunksToClaim);
    }
}
