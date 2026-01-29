package com.oxywire.oxytowns.command.commands.town.sub;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.Hidden;
import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.command.annotation.AcceptConfirmation;
import com.oxywire.oxytowns.command.annotation.CreateConfirmation;
import com.oxywire.oxytowns.command.annotation.MustBeInTown;
import com.oxywire.oxytowns.command.annotation.SendersTown;
import com.oxywire.oxytowns.config.Config;
import com.oxywire.oxytowns.config.Messages;
import com.oxywire.oxytowns.entities.impl.town.Town;
import com.oxywire.oxytowns.entities.types.perms.Permission;
import com.oxywire.oxytowns.events.TownUnclaimEvent;
import com.oxywire.oxytowns.utils.ChunkPosition;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import org.bukkit.entity.Player;

public final class UnclaimCommand {

    private final OxyTownsPlugin plugin;

    public UnclaimCommand(final OxyTownsPlugin plugin) {
        this.plugin = plugin;
    }

    @CommandMethod("town|t unclaim confirm")
    @CommandDescription("Confirm unclaiming a chunk")
    @AcceptConfirmation("player_unclaim")
    @Hidden
    @MustBeInTown
    public void onUnclaimConfirm(final Player sender, final @SendersTown Town town) {
        this.plugin.getOxyTownsApi().tryUnclaim(sender, town, true);
    }

    @CommandMethod("town|t unclaim")
    @CommandDescription("Unclaim a chunk")
    @CreateConfirmation("player_unclaim")
    @MustBeInTown
    public void onUnclaim(final Player sender, final @SendersTown Town town) {
        final Messages messages = Messages.get();
        final ChunkPosition chunkPosition = ChunkPosition.chunkPosition(sender.getLocation().getChunk());

        if (!town.hasClaimed(chunkPosition)) {
            messages.getTown().getUnclaim().getNotClaimed().send(sender);
            return;
        }

        if (!town.hasPermission(sender.getUniqueId(), Permission.CLAIM_UNCLAIM)) {
            messages.getTown().getNoPermissionClaim().send(sender);
            return;
        }

        if (town.hasOutpost(chunkPosition)) {
            // Check if it's an outpost
            final Config config = Config.get();
            final double outpostRefund = config.getOutpostRefund();
            messages.getTown().getUnclaim().getConfirmOutpostUnclaim().send(sender,
                Formatter.number("refund", outpostRefund));
        } else if (town.getHome() != null && chunkPosition.contains(town.getHome())) {
            // Check if it's home chunk
            messages.getTown().getUnclaim().getConfirmHomeBlockUnclaim().send(sender);
        } else {
            // It's a regular claim. Unclaim and tell them they unclaimed it.
            if (!town.unclaimChunk(chunkPosition, sender, TownUnclaimEvent.UnclaimCause.PLAYER)) {
                messages.getTown().getUnclaim().getUnclaimCancelled().send(sender);
                return;
            }
            messages.getTown().getUnclaim().getUnclaimSuccess().send(sender);
        }
    }
}
