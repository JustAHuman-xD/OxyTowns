package com.oxywire.oxytowns.events;

import com.oxywire.oxytowns.entities.impl.town.Town;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TownCreateEvent extends CancellableTownEvent {
    private final static HandlerList HANDLER_LIST = new HandlerList();

    /**
     * The player who created the town, if applicable
     */
    @Getter @Nullable
    private final Player player;

    public TownCreateEvent(final Town town, final @Nullable Player player) {
        super(town);
        this.player = player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
