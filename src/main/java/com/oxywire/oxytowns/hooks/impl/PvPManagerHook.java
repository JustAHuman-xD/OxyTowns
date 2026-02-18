package com.oxywire.oxytowns.hooks.impl;

import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.hooks.Hooks;
import com.oxywire.oxytowns.hooks.PluginHook;
import me.chancesd.pvpmanager.player.CombatPlayer;
import org.bukkit.entity.Player;

public class PvPManagerHook implements PluginHook {
    @Override
    public void whenEnabled(OxyTownsPlugin plugin) {}

    public boolean isInCombat(Player player) {
        return CombatPlayer.get(player).isInCombat();
    }

    public boolean hasPvPEnabled(Player player) {
        return CombatPlayer.get(player).hasPvPEnabled();
    }

    @Override
    public String getPluginName() {
        return "PvPManager";
    }
}
