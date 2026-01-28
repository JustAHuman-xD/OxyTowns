package com.oxywire.oxytowns.command.commands.town;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.command.annotation.MustBeInTown;
import com.oxywire.oxytowns.command.annotation.SendersTown;
import com.oxywire.oxytowns.config.Messages;
import com.oxywire.oxytowns.entities.impl.town.Town;
import com.oxywire.oxytowns.entities.types.Role;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;

public class TownChatCommand {

    private static final NamespacedKey TOWNCHAT_HIDDEN = new NamespacedKey(OxyTownsPlugin.get(), "townchat_hidden");
    private static final NamespacedKey TOWNCHAT_TOGGLED = new NamespacedKey(OxyTownsPlugin.get(), "townchat_toggled");
    private static final NamespacedKey TOWNCHATSPY_TOGGLED = new NamespacedKey(OxyTownsPlugin.get(), "townchatspy_toggled");

    @CommandMethod("ignoretownchat|itc")
    @MustBeInTown
    public void onToggleIgnore(final Player sender) {
        toggleIgnoringTownChat(sender);
    }

    @CommandMethod("townchatspy|tcs")
    @CommandPermission("oxytowns.townchatspy")
    public void onToggleSpy(final Player sender) {
        toggleTownChatSpy(sender);
    }

    @CommandMethod("townchat|tc")
    @MustBeInTown
    public void onDefault(final Player sender, final @SendersTown Town town) {
        toggleTownChat(sender);
    }

    @CommandMethod("townchat|tc <message>")
    @MustBeInTown
    public void onSpecific(final Player sender, final @SendersTown Town town, @Argument("message") @Greedy final String message) {
        sendTownChatMessage(sender, town, Component.text(message));
    }

    @CommandMethod("town|t chat")
    @MustBeInTown
    public void onChat(final Player sender, final @SendersTown Town town) {
        toggleTownChat(sender);
    }

    @CommandMethod("town|t chat <message>")
    @MustBeInTown
    public void onChat(final Player sender, final @SendersTown Town town, @Argument("message") @Greedy final String message) {
        sendTownChatMessage(sender, town, Component.text(message));
    }

    public static void handleChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (hasTownChatEnabled(player)) {
            Town town = OxyTownsPlugin.get().getTownCache().getTownByPlayer(player).orElse(null);
            if (town != null) {
                sendTownChatMessage(player, town, event.message());
                event.setCancelled(true);
            }
        }
    }

    private static void sendTownChatMessage(Player sender, Town town, Component message) {
        Role role = town.getRole(sender.getUniqueId());
        TagResolver[] placeholders = new TagResolver[] {
            Placeholder.unparsed("town", town.getName()),
            Placeholder.unparsed("role", role != null ? StringUtils.capitalize(role.name().toLowerCase(Locale.ROOT)) : "Outsider"),
            Placeholder.unparsed("sender", sender.getName()),
            Placeholder.component("message", message)
        };
        Messages.Town.Chat chat = Messages.get().getTown().getChat();
        chat.getFormat().send(town.filterAudience(member -> !(member instanceof Player player) || player == sender || !isIgnoringTownChat(player)), placeholders);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != sender && (!town.isMemberOrOwner(player.getUniqueId()) || isIgnoringTownChat(player)) && hasTownChatSpyEnabled(player)) {
                chat.getSpyFormat().send(player, placeholders);
            }
        }
    }

    public static void toggleTownChat(Player player) {
        if (hasTownChatEnabled(player)) {
            player.getPersistentDataContainer().set(TOWNCHAT_TOGGLED, PersistentDataType.BOOLEAN, false);
            Messages.get().getTown().getChat().getToggleOff().send(player);
        } else {
            player.getPersistentDataContainer().set(TOWNCHAT_TOGGLED, PersistentDataType.BOOLEAN, true);
            Messages.get().getTown().getChat().getToggleOn().send(player);
        }
    }

    public static void toggleIgnoringTownChat(Player player) {
        if (isIgnoringTownChat(player)) {
            player.getPersistentDataContainer().remove(TOWNCHAT_HIDDEN);
            Messages.get().getTown().getChat().getIgnoreToggleOff().send(player);
        } else {
            player.getPersistentDataContainer().set(TOWNCHAT_HIDDEN, PersistentDataType.BOOLEAN, true);
            Messages.get().getTown().getChat().getIgnoreToggleOn().send(player);
        }
    }

    public static void toggleTownChatSpy(Player player) {
        if (hasTownChatSpyEnabled(player)) {
            player.getPersistentDataContainer().set(TOWNCHATSPY_TOGGLED, PersistentDataType.BOOLEAN, false);
            Messages.get().getTown().getChat().getSpyToggleOff().send(player);
        } else {
            player.getPersistentDataContainer().set(TOWNCHATSPY_TOGGLED, PersistentDataType.BOOLEAN, true);
            Messages.get().getTown().getChat().getSpyToggleOn().send(player);
        }
    }

    public static boolean hasTownChatEnabled(Player player) {
        return player.getPersistentDataContainer().getOrDefault(TOWNCHAT_TOGGLED, PersistentDataType.BOOLEAN, false);
    }

    public static boolean isIgnoringTownChat(Player player) {
        return player.getPersistentDataContainer().has(TOWNCHAT_HIDDEN, PersistentDataType.BOOLEAN);
    }

    public static boolean hasTownChatSpyEnabled(Player player) {
        return player.getPersistentDataContainer().getOrDefault(TOWNCHATSPY_TOGGLED, PersistentDataType.BOOLEAN, false);
    }
}
