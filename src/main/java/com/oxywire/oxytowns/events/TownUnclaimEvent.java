package com.oxywire.oxytowns.events;

import com.oxywire.oxytowns.command.commands.admin.misc.TownUnclaimCommand;
import com.oxywire.oxytowns.command.commands.town.sub.UnclaimCommand;
import com.oxywire.oxytowns.entities.impl.town.Town;
import com.oxywire.oxytowns.utils.ChunkPosition;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TownUnclaimEvent extends CancellableTownEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Getter
    private final ChunkPosition chunk;
    /**
     * The player who's unclaiming the land, if applicable
     */
    @Getter @Nullable
    private final Player player;
    @Getter
    private final UnclaimCause cause;

    public TownUnclaimEvent(@NotNull Town town, @NotNull ChunkPosition chunk, @Nullable Player player, @NotNull UnclaimCause cause) {
        super(town);
        this.chunk = chunk;
        this.player = player;
        this.cause = cause;
    }

    public enum UnclaimCause {
        /**
         * For unclaims caused by upkeep failure
         */
        UPKEEP,
        /**
         * For unclaims made by admins
         * See {@link TownUnclaimCommand} for example
         */
        ADMIN,
        /**
         * For unclaims made by players
         * See {@link UnclaimCommand} for example
         */
        PLAYER,
        /**
         * For unclaims made by plugins or other non-player, non-admin sources
         */
        OTHER
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
