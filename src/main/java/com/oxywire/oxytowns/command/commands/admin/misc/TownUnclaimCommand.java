package com.oxywire.oxytowns.command.commands.admin.misc;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import com.oxywire.oxytowns.cache.TownCache;
import com.oxywire.oxytowns.config.Messages;
import com.oxywire.oxytowns.entities.impl.town.Town;
import com.oxywire.oxytowns.events.TownUnclaimEvent;
import com.oxywire.oxytowns.utils.ChunkPosition;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

public final class TownUnclaimCommand {

    private final TownCache townCache;

    public TownUnclaimCommand(final TownCache townCache) {
        this.townCache = townCache;
    }

    @CommandMethod("townadmin|ta unclaim")
    @CommandDescription("Force unclaim the land you're standing on")
    @CommandPermission("oxytowns.admin")
    public void onUnclaim(final Player sender) {
        final Messages messages = Messages.get();
        final Town wantedTown = this.townCache.getTownByLocation(sender.getLocation());

        if (wantedTown == null) {
            messages.getTown().getUnclaim().getNotClaimed().send(sender);
            return;
        }

        if (!wantedTown.unclaimChunk(ChunkPosition.chunkPosition(sender.getLocation().getChunk()), sender, TownUnclaimEvent.UnclaimCause.ADMIN)) {
            messages.getTown().getUnclaim().getUnclaimCancelled().send(sender);
            return;
        }
        messages.getAdmin().getTown().getChunkUnclaimSuccess().send(sender, Placeholder.unparsed("town", wantedTown.getName()));
    }
}
