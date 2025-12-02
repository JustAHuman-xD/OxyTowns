package com.oxywire.oxytowns.menu.town;

import com.oxywire.oxytowns.entities.impl.town.Town;
import com.oxywire.oxytowns.menu.Menu;
import com.oxywire.oxytowns.menu.MenuElement;
import com.oxywire.oxytowns.menu.PagedMenu;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import lombok.AllArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.function.BiConsumer;

@AllArgsConstructor
public final class OutpostsMenu extends PagedMenu {
    @Setter
    private static BiConsumer<Player, Location> teleportFunction = Entity::teleportAsync;

    private final Town town;

    public static void open(final Player player, final Town town) {
        Menu.builder(new OutpostsMenu(town)).build().open(player);
    }

    @Override
    public ClickableItem[] createPaged(final Player player, final InventoryContents contents) {
        final Map<String, MenuElement> elements = getConfig().getElements();

        Menu.set(contents, elements.get("go-home"), e -> TownMainMenu.open(player, this.town));

        return this.town.getOutpostChunks().stream()
            .map(chunk -> elements.get("outpost").getElement(
                e -> teleportFunction.accept(player, chunk.getBukkitLocation()),
                Formatter.number("x", chunk.getX()),
                Formatter.number("z", chunk.getZ()),
                Placeholder.unparsed("world", chunk.getWorld())
            ))
            .toArray(ClickableItem[]::new);
    }
}
