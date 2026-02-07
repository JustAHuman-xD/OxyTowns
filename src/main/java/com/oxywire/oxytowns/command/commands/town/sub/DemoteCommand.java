package com.oxywire.oxytowns.command.commands.town.sub;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import com.oxywire.oxytowns.command.annotation.MustBeInTown;
import com.oxywire.oxytowns.command.annotation.SendersTown;
import com.oxywire.oxytowns.entities.impl.town.Town;
import com.oxywire.oxytowns.menu.town.MembersMenu;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class DemoteCommand {
    @CommandMethod("town|t demote <player>")
    @CommandDescription("Demote a player in your town")
    @MustBeInTown
    public void onBan(
        final Player sender,
        final @SendersTown Town town,
        final @Argument(value = "player", suggestions = "town:members") OfflinePlayer offlinePlayer
    ) {
        MembersMenu.role(sender, town, offlinePlayer.getUniqueId(), false, false);
    }
}
