package com.oxywire.oxytowns.events;

import com.oxywire.oxytowns.entities.impl.town.Town;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

public abstract class CancellableTownEvent extends TownEvent implements Cancellable {
    @Getter @Setter
    protected boolean cancelled = false;

    protected CancellableTownEvent(Town town) {
        super(town);
    }
}
