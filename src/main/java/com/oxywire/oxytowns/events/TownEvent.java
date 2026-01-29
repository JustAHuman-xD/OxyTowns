package com.oxywire.oxytowns.events;

import com.oxywire.oxytowns.entities.impl.town.Town;
import lombok.Getter;
import org.bukkit.event.Event;

public abstract class TownEvent extends Event {
    @Getter
    protected final Town town;

    protected TownEvent(Town town) {
        this.town = town;
    }
}
