package com.oxywire.oxytowns.command.commands.town.sub;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import com.google.common.collect.Sets;
import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.command.annotation.MustBeInTown;
import com.oxywire.oxytowns.command.annotation.SendersTown;
import com.oxywire.oxytowns.entities.impl.town.Town;
import com.oxywire.oxytowns.utils.ChunkPosition;
import org.bukkit.entity.Player;

import java.util.Set;

public final class ClaimCommand {

    private final OxyTownsPlugin plugin;

    public ClaimCommand(final OxyTownsPlugin plugin) {
        this.plugin = plugin;
    }

    @CommandMethod("town|t claim")
    @CommandDescription("Claim a chunk for your town")
    @MustBeInTown
    public void onClaim(final Player sender, final @SendersTown Town town) {
        final ChunkPosition chunkPosition = ChunkPosition.chunkPosition(sender.getLocation());
        Set<ChunkPosition> chunksToClaim = Sets.newHashSet(chunkPosition);
        plugin.getOxyTownsApi().tryClaim(sender, town, chunksToClaim);
    }
}
