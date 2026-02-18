package com.oxywire.oxytowns.hooks.impl;

import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.config.Config;
import com.oxywire.oxytowns.hooks.PluginHook;

public class SquareMapHook implements PluginHook {
    @Override
    public void whenEnabled(OxyTownsPlugin plugin) {
        new SquareMapUpdater().runTaskTimerAsynchronously(OxyTownsPlugin.get(), 0L, 20L * Config.get().getHooks().getSquaremap().getUpdateInterval());
    }

    @Override
    public String getPluginName() {
        return "squaremap";
    }
}
