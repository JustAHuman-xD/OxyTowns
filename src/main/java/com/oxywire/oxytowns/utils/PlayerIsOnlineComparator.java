package com.oxywire.oxytowns.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class PlayerIsOnlineComparator implements Comparator<UUID> {

    // TODO: replace this with something better
    private static final Map<UUID, String> NAME_CACHE = new HashMap<>();

    public static final PlayerIsOnlineComparator INSTANCE = new PlayerIsOnlineComparator();

    @Override
    public int compare(final UUID o1, final UUID o2) {
        final boolean aOnline = Bukkit.getPlayer(o1) != null;
        final boolean bOnline = Bukkit.getPlayer(o2) != null;

        if (aOnline && !bOnline) {
            return -1;
        } else if (!aOnline && bOnline) {
            return 1;
        } else {
            final String name1 = NAME_CACHE.computeIfAbsent(o1, k -> {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(k);
                return offlinePlayer.getName() != null ? offlinePlayer.getName() : k + " (name not cached)";
            });
            final String name2 = NAME_CACHE.computeIfAbsent(o2, k -> {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(k);
                return offlinePlayer.getName() != null ? offlinePlayer.getName() : k + " (name not cached)";
            });
            return Objects.requireNonNullElse(name1, "null1").compareTo(Objects.requireNonNullElse(name2, "null2"));
        }
    }
}
