package com.oxywire.oxytowns.command.commands.admin.misc;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.Hidden;
import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.command.annotation.AcceptConfirmation;
import com.oxywire.oxytowns.command.annotation.CreateConfirmation;
import com.oxywire.oxytowns.config.Messages;
import org.bukkit.command.CommandSender;

public final class TaxCommand {

    private final OxyTownsPlugin plugin;

    public TaxCommand(final OxyTownsPlugin plugin) {
        this.plugin = plugin;
    }

    @CommandMethod("townadmin|ta upkeep confirm")
    @CommandDescription("Force collect upkeep")
    @CommandPermission("oxytowns.admin.upkeep")
    @AcceptConfirmation("admin_trigger_taxes")
    @Hidden
    public void onTaxConfirm(final CommandSender sender) {
        this.plugin.getTaxSchedule().takeTownTax();
    }

    @CommandMethod("townadmin|ta upkeep")
    @CommandDescription("Force collect upkeep")
    @CommandPermission("oxytowns.admin.upkeep")
    @CreateConfirmation("admin_trigger_taxes")
    public void onTax(final CommandSender sender) {
        Messages.get().getAdmin().getUpkeepConfirm().send(sender);
    }
}
