package com.oxywire.oxytowns.menu.town;

import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.config.Messages;
import com.oxywire.oxytowns.config.messaging.Message;
import com.oxywire.oxytowns.entities.impl.town.Town;
import com.oxywire.oxytowns.entities.types.Role;
import com.oxywire.oxytowns.entities.types.perms.Permission;
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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
public final class MembersMenu extends PagedMenu {

    // TODO: replace this with something better
    private static final Map<UUID, String> NAME_CACHE = new HashMap<>();
    private static final Map<UUID, String> LAST_ONLINE_CACHE = new HashMap<>();
    private static final DateTimeFormatter LAST_ONLINE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Town town;

    private static String getName(UUID uuid) {
        return NAME_CACHE.computeIfAbsent(uuid, k -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(k);
            return offlinePlayer.getName() != null ? offlinePlayer.getName() : k + " (name not cached)";
        });
    }

    private static String getLastOnline(UUID uuid) {
        if (Bukkit.getPlayer(uuid) != null) {
            LAST_ONLINE_CACHE.remove(uuid);
            return "Now";
        }
        return LAST_ONLINE_CACHE.computeIfAbsent(uuid, k -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(k);
            long timestamp = offlinePlayer.getLastLogin();
            if (timestamp > 0) {
                Instant instant = Instant.ofEpochMilli(timestamp);
                return LAST_ONLINE_FORMATTER.format(instant.atZone(ZoneId.systemDefault()).toLocalDate());
            }
            return "Unknown";
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
        Menu.set(contents, elements.get("ban"), e -> BansMenu.open(player, this.town));
        Menu.set(contents, elements.get("trusted"), e -> TrustedMenu.open(player, this.town));

        return this.town.getOwnerAndMembersWithRoles().entrySet().stream()
            .sorted((a, b) -> PlayerIsOnlineComparator.INSTANCE.compare(a.getKey(), b.getKey()))
            .map(member -> {
                final String name = getName(member.getKey());
                final ItemStack item = elements.get("member").getItem(
                    Placeholder.unparsed("name", Objects.requireNonNullElse(name, member.getKey().toString() + " (name not cached)")),
                    Placeholder.unparsed("role", Message.formatEnum(member.getValue())),
                    Formatter.booleanChoice("status", Bukkit.getPlayer(member.getKey()) != null),
                    Placeholder.unparsed("last-online", getLastOnline(member.getKey()))
                );
                item.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile()
                    .uuid(member.getKey()));

                return ClickableItem.of(
                    item,
                    e -> role(
                        player,
                        this.town,
                        member.getKey(),
                        e.isLeftClick(),
                        true
                    )
                );
            })
            .toArray(ClickableItem[]::new);
    }

    public static void role(final Player player, final Town town, final UUID target, final boolean promote, final boolean gui) {
        if (!town.hasPermission(player.getUniqueId(), Permission.MANAGE_ROLES)) {
            return;
        }

        if (target.equals(town.getOwner())) {
            return;
        }

        Role role = promote ? town.promotePlayer(target) : town.demotePlayer(target);
        if (role == null) {
            return;
        }

        if (gui) {
            open(player, town);
        }

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
