package com.oxywire.oxytowns.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.config.Config;
import com.oxywire.oxytowns.utils.Json;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

public class NotificationStorageManager {

    private final File dataFile;

    public NotificationStorageManager(final OxyTownsPlugin plugin) {
        this.dataFile = new File(plugin.getDataFolder(), "notifications");
    }

    public void queueNotification(UUID playerId, String id, Component notification) {
        queueNotification(playerId, id, notification, false);
    }

    public void queueNotification(UUID playerId, String id, Component notification, boolean force) {
        if (!force && !Config.get().getNotifications().isEnabled()) {
            return;
        }

        File playerFile = new File(dataFile, playerId + ".json");
        JsonObject notifications;

        try {
            if (playerFile.exists()) {
                try(BufferedReader reader = Files.newBufferedReader(playerFile.toPath(), StandardCharsets.UTF_8)) {
                    notifications = Json.GSON.fromJson(reader, JsonObject.class);
                }
            } else {
                notifications = new JsonObject();
                playerFile.getParentFile().mkdirs();
                playerFile.createNewFile();
            }

            JsonElement element = GsonComponentSerializer.gson().serializeToTree(notification);
            notifications.add(id, element);

            Files.writeString(playerFile.toPath(), Json.GSON.toJson(notifications));
        } catch (Exception e) {
            OxyTownsPlugin.get().getSLF4JLogger().error("Failed to queue notification for player: {}", playerId, e);
        }
    }

    public void sendAndConsumeNotificationsFor(Player player) {
        File playerFile = new File(dataFile, player.getUniqueId() + ".json");
        if (!playerFile.exists()) {
            return;
        }

        try(BufferedReader reader = Files.newBufferedReader(playerFile.toPath(), StandardCharsets.UTF_8)) {
            JsonObject notifications = Json.GSON.fromJson(reader, JsonObject.class);
            for (JsonElement element : notifications.asMap().values()) {
                Component component = GsonComponentSerializer.gson().deserializeFromTree(element);
                player.sendMessage(component);
            }
            playerFile.delete();
        } catch (Exception e) {
            OxyTownsPlugin.get().getSLF4JLogger().error("Failed to send queued notifications for player: {}", player.getUniqueId(), e);
        }
    }
}
