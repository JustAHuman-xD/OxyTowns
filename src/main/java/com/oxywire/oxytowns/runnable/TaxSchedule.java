package com.oxywire.oxytowns.runnable;

import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.config.Config;
import com.oxywire.oxytowns.config.Messages;
import com.oxywire.oxytowns.config.UpkeepTimes;
import com.oxywire.oxytowns.entities.impl.town.Town;
import com.oxywire.oxytowns.events.TaxCollectionEvent;
import com.oxywire.oxytowns.utils.ChunkPosition;
import com.oxywire.oxytowns.utils.FinePosition;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TaxSchedule {

    private final OxyTownsPlugin plugin;

    public TaxSchedule(final OxyTownsPlugin plugin) {
        this.plugin = plugin;
        handleSchedule();
    }

    public static double getTownTaxValue() {
        return Config.get().getUpkeep().getTownValue();
    }

    /**
     * Take the taxes from all the towns This will also disband towns if they don't have enough
     */
    public Map.Entry<List<String>, Double> takeTownTax() {
        final Config config = Config.get();
        final Messages messages = Messages.get();

        Bukkit.getOnlinePlayers().forEach(player -> messages.getTax().getCollectionMessage().send(player));

        final Set<Town> toDelete = new HashSet<>();
        double totalTax = 0;

        for (Town town : this.plugin.getTownCache().all()) {
            double taxToTake = config.getUpkeep().getTownValue() * town.getOutpostAndClaimedChunks().size();
            totalTax += taxToTake;

            Config.Upkeep.Leniency leniency = config.getUpkeep().getLeniency();
            if (taxToTake > 0 && taxToTake > town.getBankValue() && leniency.isEnabled() && leniency.isSellOutposts() && config.getOutpostRefund() > 0) {
                while (!town.getOutpostChunks().isEmpty() && taxToTake > town.getBankValue()) {
                    FinePosition outpost = town.getOutpostChunks().iterator().next();
                    town.unclaimChunk(ChunkPosition.chunkPosition(outpost), null);
                }

                if (taxToTake <= town.getBankValue()) {
                    town.notifyOfflineMembers("town-outposts-unclaimed", messages.getNotifications().getTownOutpostsUnclaimed(),
                        Placeholder.unparsed("town", town.getName()));
                    messages.getTax().getTownOutpostsUnclaimed().send(Bukkit.getServer(), Placeholder.unparsed("town", town.getName()));
                }
            }

            if (taxToTake > 0 && taxToTake > town.getBankValue()) {
                if (leniency.isEnabled() && !town.isMissedLastUpkeep()) {
                    town.getOutpostAndClaimedChunks().forEach(chunk -> town.unclaimChunk(chunk, null));
                    town.setBankValue(0);
                    town.setMissedLastUpkeep(true);
                    town.notifyOfflineMembers("town-unclaimed", messages.getNotifications().getTownUnclaimed(), Placeholder.unparsed("town", town.getName()));
                    messages.getTax().getTownUnclaimed().send(Bukkit.getServer(), Placeholder.unparsed("town", town.getName()));
                } else {
                    toDelete.add(town);
                    town.notifyOfflineMembers("town-disbanded", messages.getNotifications().getTownDisbanded(), Placeholder.unparsed("town", town.getName()));
                    messages.getTax().getTownDisbanded().send(Bukkit.getServer(), Placeholder.unparsed("town", town.getName()));
                }
                continue;
            }

            if (taxToTake > 0) {
                town.removeWorth(taxToTake);
                town.setMissedLastUpkeep(false);
            }
        }

        toDelete.forEach(town -> {
            if (config.getUpkeep().isBackupBeforeDisband()) {
                this.plugin.getTownCache().getTownDao().backup(town, "upkeep_disband");
            }
            this.plugin.getTownCache().deleteTown(town);
        });
        return Map.entry(toDelete.stream().map(Town::getName).toList(), totalTax);
    }

    /**
     * Handles the repeating schedule for when taxes will run
     */
    private void handleSchedule() {
        Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            if (!Config.get().getUpkeep().isEnabled()) return;

            if (!isSameHour(UpkeepTimes.get().getLastUpkeepWarning(), System.currentTimeMillis())) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    OxyTownsPlugin.get().getTownCache().getTownByPlayer(player).ifPresent($ -> {
                        Messages.get().getTax().getCollectionWarning().send(player, Formatter.number("time", 60 - LocalDateTime.now().getMinute()));
                    });
                }
                UpkeepTimes.get().setLastUpkeepWarning(System.currentTimeMillis());
                OxyTownsPlugin.configManager.save(UpkeepTimes.get());
            }

            final Config.Upkeep config = Config.get().getUpkeep();
            final int currentHour = Instant.now().atZone(config.getTimezone()).getHour();
            if (config.getHour() != currentHour || isSameDay(UpkeepTimes.get().getLastUpkeep(), System.currentTimeMillis())) {
                return;
            }

            Map.Entry<List<String>, Double> taxData = this.takeTownTax();
            Bukkit.getPluginManager().callEvent(new TaxCollectionEvent(taxData.getKey(), taxData.getValue()));

            UpkeepTimes.get().setLastUpkeep(System.currentTimeMillis());
            OxyTownsPlugin.configManager.save(UpkeepTimes.get());
        }, 20, 20);
    }

    private boolean isSameDay(long m1, long m2) {
        return m1 / 86_400_000 == m2 / 86_400_000;
    }

    private boolean isSameHour(long m1, long m2) {
        return m1 / 3_600_000 == m2 / 3_600_000;
    }
}
