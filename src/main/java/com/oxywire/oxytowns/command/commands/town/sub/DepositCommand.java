package com.oxywire.oxytowns.command.commands.town.sub;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.specifier.Range;
import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.command.annotation.MustBeInTown;
import com.oxywire.oxytowns.command.annotation.SendersTown;
import com.oxywire.oxytowns.config.Config;
import com.oxywire.oxytowns.config.Messages;
import com.oxywire.oxytowns.entities.impl.town.Town;
import com.oxywire.oxytowns.entities.types.Upgrade;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

public final class DepositCommand {

    private final OxyTownsPlugin plugin;

    public DepositCommand(final OxyTownsPlugin plugin) {
        this.plugin = plugin;
    }

    @CommandMethod("town|t deposit <amount>")
    @CommandDescription("Deposit money into your town's bank")
    @MustBeInTown
    public void onDeposit(final Player sender, final @SendersTown Town town, final @Argument("amount") @Range(min = "0.01") double amount) {
        final Messages messages = Messages.get();
        if (!Config.get().getTownBank().isAccessOutsideTown() && !town.hasClaimed(sender.getLocation()) && !town.getOutpostAndClaimedChunks().isEmpty()) {
            messages.getTown().getBank().getErrorNotWithinTown().send(sender);
        } else if (town.getOutpostAndClaimedChunks().isEmpty() && town.getBankValue() >= Config.get().getClaimPrice()) {
            messages.getTown().getBank().getErrorMustClaimFirst().send(sender);
        } else if (!this.plugin.getEconomy().has(sender, amount)) {
            messages.getPlayer().getErrorCannotAffordDeposit().send(sender, Formatter.number("amount", amount));
        } else if (town.getWorth() + amount > town.getUpgradeValue(Upgrade.BANK_CAPACITY)) {
            messages.getTown().getBank().getErrorDepositPastMax().send(sender, Formatter.number("excess", (town.getWorth() + amount) - town.getUpgradeValue(Upgrade.BANK_CAPACITY)));
        } else if (this.plugin.getEconomy().withdrawPlayer(sender, amount).transactionSuccess()) {
            town.addWorth(amount);
            messages.getTown().getBank().getDepositSuccessful().send(town, Placeholder.unparsed("player", sender.getName()), Formatter.number("amount", amount));
        } else {
            messages.getPlayer().getErrorCannotAffordDeposit().send(sender);
        }
    }
}
