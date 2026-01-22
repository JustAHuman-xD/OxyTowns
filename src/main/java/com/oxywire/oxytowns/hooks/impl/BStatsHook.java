package com.oxywire.oxytowns.hooks.impl;

import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.hooks.PluginHook;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;

public final class BStatsHook implements PluginHook {

    public void whenEnabled(OxyTownsPlugin plugin) {
        final Metrics metrics = new Metrics(plugin, 19551);
        metrics.addCustomChart(new SingleLineChart("towns", () -> plugin.getTownCache().getTowns().size()));
    }

    @Override
    public String getPluginName() {
        return ""; // Not tied to a specific plugin
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
