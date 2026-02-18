package com.oxywire.oxytowns.menu.town;

import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.config.Messages;
import com.oxywire.oxytowns.config.messaging.Message;
import com.oxywire.oxytowns.entities.impl.town.Town;
import com.oxywire.oxytowns.entities.types.Role;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
public final class MembersMenu extends PagedMenu {

    // TODO: replace this with something better
    private static final Map<UUID, String> NAME_CACHE = new HashMap<>();

    private final Town town;

    private static String getName(UUID uuid) {
        return NAME_CACHE.computeIfAbsent(uuid, k -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(k);
            return offlinePlayer.getName() != null ? offlinePlayer.getName() : k + " (name not cached)";
        });
    }

    public static void open(final Player player, final Town town) {
        Menu.builder(new MembersMenu(town))
            .build()
            .open(player, Menu.INVENTORY_MANAGER.getContents(player).map(i -> i.pagination().getPage()).orElse(0));
    }

    @Override
    public ClickableItem[] createPaged(final Player player, final InventoryContents contents) {
        final Map<String, MenuElement> elements = getConfig().getElements();

        Menu.set(contents, elements.get("go-home"), e -> TownMainMenu.open(player, this.town));
        Menu.set(contents, elements.get("members"), e -> {});
        Menu.set(contents, elements.get("trusted"), e -> TrustedMenu.open(player, this.town));
        Menu.set(contents, elements.get("ban"), e -> BansMenu.open(player, this.town));

        return this.town.getOwnerAndMembersWithRoles().entrySet().stream()
            .sorted((a, b) -> PlayerIsOnlineComparator.INSTANCE.compare(a.getKey(), b.getKey()))
            .map(member -> {
                final String name = getName(member.getKey());
                final ItemStack item = elements.get("member").getItem(
                    Placeholder.unparsed("name", Objects.requireNonNullElse(name, member.getKey().toString() + " (name not cached)")),
                    Placeholder.unparsed("role", Message.formatEnum(member.getValue())),
                    Formatter.booleanChoice("status", Bukkit.getPlayer(member.getKey()) != null)
                );
                item.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile()
                    .uuid(member.getKey()));

                return ClickableItem.of(
                    item,
                    e -> role(
                        player,
                        this.town,
                        member.getKey(),
                        e.isLeftClick()
                    )
                );
            })
            .toArray(ClickableItem[]::new);
    }

    private static void role(final Player player, final Town town, final UUID target, final boolean promote) {
        if (!town.getOwner().equals(player.getUniqueId())) {
            return;
        }

        if (target.equals(town.getOwner())) {
            return;
        }

        Role role = promote ? town.promotePlayer(target) : town.demotePlayer(target);
        if (role == null) {
            return;
        }

        open(player, town);

        Messages messages = Messages.get();
        Message message = promote ? messages.getTown().getBroadcastPromotion() : messages.getTown().getBroadcastDemotion();
        message.send(
            town,
            Placeholder.unparsed("player", getName(target)),
            Placeholder.unparsed("role", Message.formatEnum(role))
        );

        if (Bukkit.getPlayer(target) == null) {
            OxyTownsPlugin.notificationStorageManager.queueNotification(target, "town-role-changed", messages.getNotifications().getTownKicked().message(
                Placeholder.unparsed("town", town.getName()),
                Placeholder.unparsed("role", Message.formatEnum(role))
            ));
        }
    }
}
