package com.oxywire.oxytowns.events;

import com.oxywire.oxytowns.entities.impl.town.Town;
import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TownMayorChangeEvent extends TownEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Getter
    private final UUID oldMayor;
    @Getter
    private final UUID newMayor;

    public TownMayorChangeEvent(Town town, UUID oldMayor, UUID newMayor) {
        super(town);
        this.oldMayor = oldMayor;
        this.newMayor = newMayor;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
