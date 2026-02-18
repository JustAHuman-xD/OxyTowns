package com.oxywire.oxytowns.events;

import com.oxywire.oxytowns.entities.impl.town.Town;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TownRenameEvent extends CancellableTownEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Getter
    private final String oldName;
    @Getter @Setter
    private String newName;

    public TownRenameEvent(Town town, String oldName, String newName) {
        super(town);
        this.oldName = oldName;
        this.newName = newName;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
