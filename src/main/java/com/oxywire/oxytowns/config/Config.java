package com.oxywire.oxytowns.config;

import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.config.messaging.Message;
import com.oxywire.oxytowns.entities.types.PlotType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.time.ZoneId;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
@ConfigSerializable
public final class Config {

    @Setting
    private double claimPrice = 150.0;

    @Setting
    private double outpostPrice = 50_000.0;

    @Setting
    private double outpostRefund = 10_000.0;

    @Setting
    private int maxClaimRadius = 1;

    @Setting
    private Map<PlotType, Plot> plots = Map.of(
        PlotType.FARM, new Plot(EnumSet.of(Material.CARROTS, Material.POTATOES, Material.WHEAT), EnumSet.noneOf(EntityType.class), Set.of()),
        PlotType.MOB_FARM, new Plot(EnumSet.noneOf(Material.class), EnumSet.of(EntityType.COW, EntityType.PIG, EntityType.SHEEP, EntityType.CHICKEN), Set.of()),
        PlotType.ARENA, new Plot(EnumSet.noneOf(Material.class), EnumSet.of(EntityType.PLAYER), Set.of("/fly"))
    );

    @Setting
    private Map<com.oxywire.oxytowns.entities.types.Upgrade, Config.Upgrade> upgrades = new LinkedHashMap<>(Map.of(
        com.oxywire.oxytowns.entities.types.Upgrade.CLAIMS, new Config.Upgrade("Claims", Map.of(15, 10_000.0, 20, 20_000.0, 50, 30_000.0, 100, 40_000.0, 200, 50_000.0, 350, 60_000.0, 500, 70_000.0)),
        com.oxywire.oxytowns.entities.types.Upgrade.MEMBERS, new Config.Upgrade("Members", Map.of(10, 10_000.0, 25, 20_000.0, 50, 30_000.0, 75, 40_000.0, 100, 50_000.0, 150, 60_000.0, 250, 70_000.0)),
        com.oxywire.oxytowns.entities.types.Upgrade.VAULT_AMOUNT, new Config.Upgrade("Vault Amount", Map.of(2, 10_000.0, 3, 20_000.0, 4, 30_000.0, 5, 40_000.0, 6, 50_000.0, 7, 60_000.0, 8, 70_000.0)),
        com.oxywire.oxytowns.entities.types.Upgrade.OUTPOSTS, new Config.Upgrade("Outposts", Map.of(1, 10_000.0, 2, 20_000.0, 3, 30_000.0, 4, 40_000.0, 5, 50_000.0, 6, 60_000.0, 7, 70_000.0))
    ));

    @Setting
    private boolean allowPvpInWilderness = false;

    @Setting
    private boolean pvpStatusEnterExitMessages = true;

    @Setting
    private int leaveNoPvpGracePeriod = 30;

    @Setting
    private List<String> blacklistedWorlds = List.of(
        "resource_world"
    );

    @Setting
    private Upkeep upkeep = new Upkeep();

    @Setting
    private TownBank townBank = new TownBank();

    @Setting
    private TownVaults townVaults = new TownVaults();

    @Setting
    private TownBanExpel townBanExpel = new TownBanExpel();

    @Setting
    private TownChat townChat = new TownChat();

    @Setting
    private Notifications notifications = new Notifications();

    @Setting
    private Hooks hooks = new Hooks();

    public static Config get() {
        return OxyTownsPlugin.configManager.get(Config.class);
    }

    @Getter
    @ConfigSerializable
    public static final class Upkeep {

        @Setting
        private boolean enabled = true;

        @Setting
        private Leniency leniency = new Leniency();

        @Setting
        private boolean backupBeforeDisband = true;

        @Setting
        private double townValue = 25;

        @Setting
        private int hour = 12;

        @Setting
        private ZoneId timezone = ZoneId.of("America/New_York");

        @Getter
        @ConfigSerializable
        public static final class Leniency {

            @Setting
            private boolean enabled = true;

            @Setting
            private boolean sellOutposts = true;

        }

    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @ConfigSerializable
    public static final class Plot {

        @Setting
        private Set<Material> blocks = EnumSet.noneOf(Material.class);

        @Setting
        private Set<EntityType> entities = EnumSet.noneOf(EntityType.class);

        @Setting
        private Set<String> blacklistedCommands = new HashSet<>();
    }

    @Getter
    @ConfigSerializable
    public static final class TownBank {

        @Setting
        private boolean accessOutsideTown = true;
    }

    @Getter
    @ConfigSerializable
    public static final class TownVaults {

        @Setting
        private boolean accessOutsideTown = true;

        @Setting
        private int rows = 6;
    }

    @Getter
    @ConfigSerializable
    public static final class TownChat {

        @Setting
        private boolean enabled = true;

        @Setting
        private Message format = new Message().setMessage("<blue>[Town] <white><sender>: <gray><message>");

        @Setting
        private Message spyFormat = new Message().setMessage("<red>[TownSpy] <white><town>@<sender>: <gray><message>");
    }

    @Getter
    @ConfigSerializable
    public static final class TownBanExpel {

        @Setting
        private boolean enabled = true;

        @Setting
        private Mode mode = Mode.RESPAWN_POINT;

        @Setting
        private String command = "spawn <player>";

        @Setting
        private Custom custom = new Custom();

        public void expel(Player target) {
            switch (mode) {
                case WORLD_SPAWN -> target.teleportAsync(target.getWorld().getSpawnLocation());
                case RESPAWN_POINT -> {
                    Location respawnLocation = target.getRespawnLocation();
                    target.teleportAsync(respawnLocation != null ? respawnLocation : target.getWorld().getSpawnLocation());
                }
                case COMMAND -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("<player>", target.getName()));
                case CUSTOM -> {
                    World world = Bukkit.getWorld(custom.getWorld());
                    if (world == null) {
                        OxyTownsPlugin.get().getSLF4JLogger().warn("Could not expel player {} to custom location: world '{}' not found, teleporting to player world spawn instead.", target.getName(), custom.getWorld());
                        target.teleportAsync(target.getWorld().getSpawnLocation());
                        return;
                    }

                    Location location = new Location(
                        world,
                        custom.getX(),
                        custom.getY(),
                        custom.getZ(),
                        custom.getYaw(),
                        custom.getPitch()
                    );
                    target.teleportAsync(location);
                }
            }
        }

        @Getter
        @ConfigSerializable
        public static final class Custom {
            @Setting
            private String world;

            @Setting
            private double x;

            @Setting
            private double y;

            @Setting
            private double z;

            @Setting
            private float yaw;

            @Setting
            private float pitch;
        }

        public enum Mode {
            WORLD_SPAWN,
            RESPAWN_POINT,
            COMMAND,
            CUSTOM
        }

    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @ConfigSerializable
    public static final class Upgrade {
        @Setting
        private String displayName;

        @Setting
        private Map<Integer, Double> upgrade;
    }

    @Getter
    @ConfigSerializable
    public static final class Notifications {

        @Setting
        private boolean enabled = true;

        @Setting
        private int delayAfterJoin = 15;

    }

    @Getter
    @ConfigSerializable
    public static final class Hooks {

        @Setting
        private boolean bstats = true;

        @Setting
        private boolean placeholderApi = true;

        @Setting
        private Squaremap squaremap = new Squaremap();

        @Setting
        private PvPManager pvpManager = new PvPManager();

        @Getter
        @ConfigSerializable
        public static final class Squaremap {

            @Setting
            private boolean enabled = true;

            @Setting
            private int updateInterval = 30;

        }

        @Getter
        @ConfigSerializable
        public static final class PvPManager {

            @Setting
            private boolean enabled = true;

        }

    }
}
