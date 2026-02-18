package com.oxywire.oxytowns.hooks;

import com.oxywire.oxytowns.OxyTownsPlugin;
import org.bukkit.Bukkit;

public interface PluginHook {

    void whenEnabled(OxyTownsPlugin plugin);
    String getPluginName();

    default boolean isEnabled() {
        return Bukkit.getPluginManager().getPlugin(getPluginName()) != null;
    }
}
