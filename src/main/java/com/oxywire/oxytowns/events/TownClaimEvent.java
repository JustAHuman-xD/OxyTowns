package com.oxywire.oxytowns.events;

import com.oxywire.oxytowns.command.commands.admin.town.TownClaimCommand;
import com.oxywire.oxytowns.command.commands.town.sub.ClaimCommand;
import com.oxywire.oxytowns.entities.impl.town.Town;
import com.oxywire.oxytowns.utils.ChunkPosition;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TownClaimEvent extends CancellableTownEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Getter
    private final ChunkPosition chunk;
    /**
     * The player who's claiming the land, if applicable
     */
    @Getter @Nullable
    private final Player player;
    @Getter
    private final ClaimCause cause;

    public TownClaimEvent(@NotNull Town town, @NotNull ChunkPosition chunk, @Nullable Player player, @NotNull ClaimCause cause) {
        super(town);
        this.chunk = chunk;
        this.player = player;
        this.cause = cause;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public enum ClaimCause {
        /**
         * For claims made during town creation
         */
        TOWN_CREATION,
        /**
         * For claims made by admins
         * See {@link TownClaimCommand} for example
         */
        ADMIN,
        /**
         * For claims made by players
         * See {@link ClaimCommand} for example
         */
        PLAYER,
        /**
         * For claims made by plugins or other non-player, non-admin sources
         */
        OTHER
    }
}
