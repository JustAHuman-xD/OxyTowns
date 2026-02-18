package com.oxywire.oxytowns.events;

import com.oxywire.oxytowns.entities.impl.town.Town;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TownDisbandEvent extends TownEvent {
    private final static HandlerList HANDLER_LIST = new HandlerList();

    public TownDisbandEvent(Town town) {
        super(town);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
