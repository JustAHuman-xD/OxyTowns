package com.oxywire.oxytowns.hooks.impl;

import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.hooks.PluginHook;

public class PlaceholderApiHook implements PluginHook {
    @Override
    public void whenEnabled(OxyTownsPlugin plugin) {
        new OxyTownsPapiExpansion().register();
    }

    @Override
    public String getPluginName() {
        return "PlaceholderAPI";
    }
}
