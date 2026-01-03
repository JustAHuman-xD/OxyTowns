package com.oxywire.oxytowns.menu.plot;

import com.oxywire.oxytowns.entities.impl.plot.Plot;
import com.oxywire.oxytowns.entities.impl.town.Town;
import com.oxywire.oxytowns.menu.Menu;
import com.oxywire.oxytowns.menu.MenuElement;
import com.oxywire.oxytowns.menu.PagedMenu;
import com.oxywire.oxytowns.utils.PlayerIsOnlineComparator;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
public final class PlotMembersMenu extends PagedMenu {

    // TODO: replace this with something better
    private static final Map<UUID, String> NAME_CACHE = new HashMap<>();

    private final Town town;
    private final Plot plot;

    public static void open(final Player player, final Town town, final Plot plot) {
        Menu.builder(new PlotMembersMenu(town, plot)).build().open(player);
    }

    @Override
    public ClickableItem[] createPaged(final Player player, final InventoryContents contents) {
        final Map<String, MenuElement> elements = getConfig().getElements();

        Menu.set(contents, elements.get("go-home"), e -> PlotMenu.open(player, this.town, this.plot));

        return this.plot.getAssignedMembers().stream()
            .sorted(PlayerIsOnlineComparator.INSTANCE)
            .map(playerId -> {
                final String name = NAME_CACHE.computeIfAbsent(playerId, k -> {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(k);
                    return offlinePlayer.getName() != null ? offlinePlayer.getName() : k + " (name not cached)";
                });
                final ItemStack item = elements.get("member").getItem(
                    Placeholder.unparsed("name", Objects.requireNonNullElse(name, playerId.toString() + " (name not cached)")),
                    Formatter.booleanChoice("status", Bukkit.getPlayer(playerId) != null)
                );
                item.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile()
                    .uuid(playerId));

                return ClickableItem.empty(item);
            })
            .toArray(ClickableItem[]::new);
    }
}
