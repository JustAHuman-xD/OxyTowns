package com.oxywire.oxytowns.listeners;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import com.google.common.collect.Sets;
import com.oxywire.oxytowns.OxyTownsPlugin;
import com.oxywire.oxytowns.cache.TownCache;
import com.oxywire.oxytowns.config.Config;
import com.oxywire.oxytowns.config.Messages;
import com.oxywire.oxytowns.config.messaging.Message;
import com.oxywire.oxytowns.entities.impl.plot.Plot;
import com.oxywire.oxytowns.entities.impl.town.Town;
import com.oxywire.oxytowns.entities.types.PlotType;
import com.oxywire.oxytowns.entities.types.perms.Permission;
import com.oxywire.oxytowns.entities.types.settings.Setting;
import com.oxywire.oxytowns.hooks.Hooks;
import com.oxywire.oxytowns.hooks.impl.PvPManagerHook;
import com.oxywire.oxytowns.utils.ChunkPosition;
import io.papermc.paper.entity.TeleportFlag;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrushableBlock;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WaterMob;
import org.bukkit.entity.WindCharge;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

@SuppressWarnings("unused")
public class NewEventsHandler implements Listener {

    private final TownCache cache = OxyTownsPlugin.get().getTownCache();

    private static final Set<Material> CHEST_MATERIALS = EnumSet.of(Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL, Material.CHISELED_BOOKSHELF);
    private static final Set<Material> FURNACE_MATERIALS = EnumSet.of(Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER, Material.CAMPFIRE,
        Material.SOUL_CAMPFIRE);
    private static final Set<Material> REDSTONE_MATERIALS = Sets.newHashSet(Material.DISPENSER, Material.DROPPER, Material.HOPPER, Material.DAYLIGHT_DETECTOR,
        Material.REPEATER, Material.COMPARATOR, Material.NOTE_BLOCK, Material.JUKEBOX, Material.CRAFTER);
    private static final Set<Material> DOOR_MATERIALS = EnumSet.noneOf(Material.class);
    private static final Set<Material> ENTITY_INTERACT_SETS = EnumSet.of(Material.FARMLAND);
    private static final Set<Material> INTERACT_SETS = EnumSet.of(Material.PUMPKIN, Material.CAKE, Material.CAVE_VINES_PLANT, Material.CAVE_VINES, Material.SWEET_BERRY_BUSH, Material.RESPAWN_ANCHOR, Material.DECORATED_POT, Material.FARMLAND, Material.SCULK_SENSOR, Material.CALIBRATED_SCULK_SENSOR);
    private static final Set<EntityType> PERMITTED_CHANGERS = EnumSet.of(EntityType.PLAYER, EntityType.VILLAGER, EntityType.TURTLE, EntityType.BEE, EntityType.LIGHTNING_BOLT, EntityType.FALLING_BLOCK, EntityType.SPLASH_POTION, EntityType.LINGERING_POTION);
    private static final Set<Material> PERMITTED_CHANGES = EnumSet.of(Material.BIG_DRIPLEAF);

    static {
        CHEST_MATERIALS.addAll(Tag.SHULKER_BOXES.getValues());
        CHEST_MATERIALS.addAll(Tag.SHULKER_BOXES.getValues());
        DOOR_MATERIALS.addAll(Tag.DOORS.getValues());
        DOOR_MATERIALS.addAll(Tag.TRAPDOORS.getValues());
        DOOR_MATERIALS.addAll(Tag.FENCE_GATES.getValues());
        ENTITY_INTERACT_SETS.addAll(DOOR_MATERIALS);
        INTERACT_SETS.addAll(Tag.CANDLES.getValues());
        INTERACT_SETS.addAll(Tag.FLOWER_POTS.getValues());
        INTERACT_SETS.remove(Material.FLOWER_POT); // ?
        INTERACT_SETS.addAll(Tag.LOGS.getValues());
        INTERACT_SETS.add(Material.DRAGON_EGG);
    }

    private final Map<UUID, Long> pvpGracePeriod = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Config.Notifications config = Config.get().getNotifications();
        if (!config.isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        Bukkit.getAsyncScheduler().runDelayed(OxyTownsPlugin.get(), task -> {
            if (player.isOnline()) {
                OxyTownsPlugin.notificationStorageManager.sendAndConsumeNotificationsFor(player);
            }
        }, config.getDelayAfterJoin(), TimeUnit.SECONDS);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!cache.isBypassing(event.getPlayer()) && cannotInteract(event.getPlayer(), event.getBlock().getLocation(), Permission.BLOCK_BREAK, event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak$0(BlockBreakEvent event) {
        handleFarmland(event.getPlayer(), event);
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player player
            && !cache.isBypassing(player)
            && cannotInteract(player, event.getEntity().getLocation(), Permission.BLOCK_BREAK, event.getEntity())
        ) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreakByEntity$1(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Projectile projectile
            && projectile.getShooter()  != null
            && projectile.getShooter() instanceof Player player
            && !cache.isBypassing(player)
            && cannotInteract(player, event.getEntity().getLocation(), Permission.BLOCK_BREAK, event.getEntity())
        ) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity$3(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Hanging)) return;

        // Figure the attacker
        Player attacker = null;
        if (event.getDamager() instanceof Player player) attacker = player;
        else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) attacker = player;
        if (attacker == null) return;

        if (!cache.isBypassing(attacker) && cannotInteract(attacker, event.getEntity().getLocation(), Permission.BLOCK_BREAK, event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Enemy)) return;

        // Figure the attacker
        Player attacker = null;
        if (event.getDamager() instanceof Player player) attacker = player;
        else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) attacker = player;
        if (attacker == null) return;

        // Don't proc if it's wilderness
        Town town = cache.getTownByLocation(event.getEntity().getLocation());
        if (town == null) return;

        Plot plot = town.getPlot(event.getEntity().getLocation());
        // Always allow players to damage hostile mobs, because they'd otherwise be invulnerable
        if (event.getEntity() instanceof Monster) return;
        // Allow mobs if it's a mob farm plot
        // Or if the town toggle is on
        if (plot != null && plot.getType() == PlotType.MOB_FARM) return;
        else if (town.getToggle(Setting.MOBS)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Projectile projectile
            && projectile.getShooter() != null
            && projectile.getShooter() instanceof Player player
            && !cache.isBypassing(player)
            && Tag.CAMPFIRES.isTagged(event.getBlock().getType())
            && cannotInteract(player, event.getBlock().getLocation(), Permission.BLOCK_BREAK, event.getBlock())
        ) {
            event.setCancelled(true);
        }
    }

    // Flying into decorated pots
    @EventHandler
    public void onEntityChangeBlock$0(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Firework firework
            && firework.getShooter() != null
            && firework.getShooter() instanceof Player player
            && !cache.isBypassing(player)
            && cannotInteract(player, event.getBlock().getLocation(), Permission.BLOCK_BREAK, event.getBlock())
        ) {
            event.setCancelled(true);
        }
    }

    // Weaving potion effect
    @EventHandler
    public void onEntityChangeBlock$1(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Player player
            && !cache.isBypassing(player)
            && event.getTo() == Material.COBWEB
            && cannotInteract(player, event.getBlock().getLocation(), Permission.BLOCK_PLACE, event.getBlock())
        ) {
            event.setCancelled(true);
        }
    }

    // Brushable blocks
    public void onEntityChangeBlock$2(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Player player
            && !cache.isBypassing(player)
            && event.getBlock().getState(false) instanceof BrushableBlock
            && cannotInteract(player, event.getBlock().getLocation(), Permission.BLOCK_BREAK, event.getBlock())
        ) {
            event.setCancelled(true);
        }
    }

    // Mob Griefing
    @EventHandler
    public void onEntityChangeBlock$3(EntityChangeBlockEvent event) {
        if (!PERMITTED_CHANGERS.contains(event.getEntityType())
            && !PERMITTED_CHANGES.contains(event.getTo())) {
            Town town = cache.getTownByLocation(event.getBlock().getLocation());
            if (town != null && !town.getToggle(Setting.GRIEF)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Town town = cache.getTownByLocation(event.getLocation());
        if (town != null && !town.getToggle(Setting.GRIEF)) {
            event.setCancelled(true);
        }
    }

    // Raids
    @EventHandler
    public void onRaidTrigger(RaidTriggerEvent event) {
        Town town = cache.getTownByLocation(event.getPlayer().getLocation());
        if (town != null
            && (!town.getToggle(Setting.RAIDS) || cannotInteract(event.getPlayer(), event.getPlayer().getLocation(), Permission.RAIDS, null))) {
            event.setCancelled(true);
        }
    }

    // Projectiles
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player player
            && !cache.isBypassing(player)
            && cannotInteract(player, event.getEntity().getLocation(), Permission.PROJECTILES, event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (!cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getBlockClicked().getLocation(), Permission.BLOCK_BREAK, event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getBlock().getLocation(), Permission.BLOCK_PLACE, event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.LECTERN && Tag.ITEMS_LECTERN_BOOKS.isTagged(event.getItemInHand().getType())) {
            return;
        }

        if (!cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getBlock().getLocation(), Permission.BLOCK_PLACE, event.getBlockPlaced())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityInteract(EntityInteractEvent event) {
        if (event.getEntity() instanceof Villager || !ENTITY_INTERACT_SETS.contains(event.getBlock().getType())) {
            return;
        }

        Location location = event.getBlock().getLocation();
        Town town = cache.getTownByLocation(location);
        if (town == null) {
            return;
        }

        switch(town.getEntityInteractionSetting()) {
            case NONE -> event.setCancelled(true);
            case TAMED -> {
                if (event.getEntity() instanceof Tameable tameable && tameable.isTamed()
                    && !cache.isBypassing(tameable.getOwnerUniqueId())
                    && cannotInteract(tameable.getOwnerUniqueId(), location, Permission.BLOCK_BREAK, event.getBlock())) {
                    event.setCancelled(true);
                }
            }
            case ALL -> {}
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null
            && INTERACT_SETS.contains(event.getClickedBlock().getType())
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getClickedBlock().getLocation(), Permission.BLOCK_BREAK, event.getClickedBlock())
        ) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        if (event.getPlayer() != null
        && !cache.isBypassing(event.getPlayer())
        && cannotInteract(event.getPlayer(), event.getEntity().getLocation(), Permission.BLOCK_PLACE, event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Hanging
        && !cache.isBypassing(event.getPlayer())
        && cannotInteract(event.getPlayer(), event.getRightClicked().getLocation(), Permission.BLOCK_PLACE, event.getRightClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract$0(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && (
                Tag.FLOWERS.isTagged(event.getClickedBlock().getType())
                    || (event.hasItem() && event.getMaterial() == Material.BONE_MEAL)
                    || MaterialSetTag.ALL_SIGNS.isTagged(event.getClickedBlock().getType())
            )
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getClickedBlock().getLocation(), Permission.BLOCK_PLACE, event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getBlockClicked().getLocation(), Permission.BLOCK_PLACE, event.getBlockClicked())) {
            event.setCancelled(true);
        }
    }

    // Chest Access
    @EventHandler
    public void onPlayerInteract$1(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && CHEST_MATERIALS.contains(event.getClickedBlock().getType())
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getClickedBlock().getLocation(), Permission.CHESTS, event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChestCartInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof StorageMinecart
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getRightClicked().getLocation(), Permission.CHESTS, event.getRightClicked())) {
            event.setCancelled(true);
        }
    }

    // Furnace Access
    @EventHandler
    public void onPlayerInteract$2(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && FURNACE_MATERIALS.contains(event.getClickedBlock().getType())
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getClickedBlock().getLocation(), Permission.FURNACES, event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    // Door Access
    @EventHandler
    public void onPlayerInteract$3(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && DOOR_MATERIALS.contains(event.getClickedBlock().getType())
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getClickedBlock().getLocation(), Permission.DOORS, event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    // Buttons & Lever Access
    @EventHandler
    public void onPlayerInteract$4(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && (event.getClickedBlock().getType() == Material.LEVER || Tag.BUTTONS.isTagged(event.getClickedBlock().getType()))
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getClickedBlock().getLocation(), Permission.BUTTONS, event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    // Pressure Plates
    @EventHandler
    public void onPlayerInteract$5(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL
            && Tag.PRESSURE_PLATES.isTagged(event.getClickedBlock().getType())
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getClickedBlock().getLocation(), Permission.PLATES, event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    // Anvils
    @EventHandler
    public void onPlayerInteract$6(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && event.getClickedBlock().getType() == Material.ANVIL
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getClickedBlock().getLocation(), Permission.ANVIL, event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    // Brewing Access
    @EventHandler
    public void onPlayerInteract$7(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && (event.getClickedBlock().getType() == Material.BREWING_STAND || Tag.CAULDRONS.isTagged(event.getClickedBlock().getType()))
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getClickedBlock().getLocation(), Permission.BREWING, event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    // Animal Welfare

    @EventHandler
    public void onPlayerInteractEntity$0(PlayerInteractEntityEvent event) {
        if ((event.getRightClicked() instanceof Animals || event.getRightClicked() instanceof Allay)
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getRightClicked().getLocation(), Permission.ANIMALS, event.getRightClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSheepDyeWool(SheepDyeWoolEvent event) {
        if (event.getPlayer() != null
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getEntity().getLocation(), Permission.ANIMALS, event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        if (!cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getEntity().getLocation(), Permission.LEASH, event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
        if (event.getReason() == EntityTargetEvent.TargetReason.TEMPT
            && event.getTarget() instanceof Player player
            && !cache.isBypassing(player)
            && cannotInteract(player, event.getEntity().getLocation(), Permission.LEASH, event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerUnleashEntity(PlayerUnleashEntityEvent event) {
        if (!cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getEntity().getLocation(), Permission.LEASH, event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        if (!cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getEntity().getLocation(), Permission.ANIMALS, event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Animals) && !(event.getEntity() instanceof WaterMob)) return;
        if (!(event.getDamageSource().getCausingEntity() instanceof Player player)) return;

        if (!cache.isBypassing(player) && cannotInteract(player, event.getEntity().getLocation(), Permission.ANIMALS, event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity$0(EntityDamageByEntityEvent event) {
        if ((event.getEntity() instanceof Animals || event.getEntity() instanceof WaterMob)) {
            final Entity attacker = event.getDamager();
            final Entity target = event.getEntity();

            if (attacker instanceof Player player && !cache.isBypassing(player) && cannotInteract(player, target.getLocation(), Permission.ANIMALS, target)) {
                event.setCancelled(true);
            } else if (attacker instanceof Projectile projectile
                && projectile.getShooter() instanceof Player player
                && !cache.isBypassing(player)
                && cannotInteract(player, target.getLocation(), Permission.ANIMALS, target)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract$8(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && (event.getClickedBlock().getType() == Material.BEEHIVE || event.getClickedBlock().getType() == Material.BEE_NEST)
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getClickedBlock().getLocation(), Permission.ANIMALS, event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract$14(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && event.getItem() != null
            && MaterialTags.SPAWN_EGGS.isTagged(event.getItem())
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getClickedBlock().getLocation(), Permission.ANIMALS, event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketEntity(PlayerBucketEntityEvent event) {
        if (!cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getEntity().getLocation(), Permission.ANIMALS, event.getEntity())) {
            event.setCancelled(true);
        }
    }

    // Vehicles

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player player
            && !(event.getVehicle() instanceof Tameable tameable && tameable.isTamed() && player.getUniqueId().equals(tameable.getOwnerUniqueId()))
            && !cache.isBypassing(player)
            && cannotInteract(player, event.getVehicle().getLocation(), Permission.VEHICLES, event.getVehicle())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent event) {
        if ((event.getEntity() instanceof Boat
            || event.getEntity() instanceof Minecart)
            && event.getPlayer() != null
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getEntity().getLocation(), Permission.BLOCK_PLACE, event.getEntity())
        ) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (event.getAttacker() instanceof Player player
            && !cache.isBypassing(player)
            && cannotInteract(player, event.getVehicle().getLocation(), Permission.VEHICLES, event.getVehicle())
        ) {
            event.setCancelled(true);
        }
    }

    //todo check rest of animal perms ?
    @EventHandler
    public void onPlayerInteractEntity$1(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Animals
        && event.getRightClicked() instanceof Vehicle
        && !(event.getRightClicked() instanceof Pig)
        && !cache.isBypassing(event.getPlayer())
        && cannotInteract(event.getPlayer(), event.getRightClicked().getLocation(), Permission.VEHICLES, event.getRightClicked())) {
            event.setCancelled(true);
        }
    }

    // Redstone
    @EventHandler
    public void onPlayerInteract$9(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && REDSTONE_MATERIALS.contains(event.getClickedBlock().getType())
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getClickedBlock().getLocation(), Permission.REDSTONE, event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHopperCartInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof HopperMinecart
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getRightClicked().getLocation(), Permission.REDSTONE, event.getRightClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTriggerSensor(BlockReceiveGameEvent event) {
        if (event.getEntity() instanceof Player player
            && !cache.isBypassing(player)
            && cannotInteract(player, event.getBlock().getLocation(), Permission.REDSTONE, event.getBlock())) {
            event.setCancelled(true);
        }
    }

    // Tripwires
    @EventHandler
    public void onPlayerInteract$10(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL
            && event.getClickedBlock().getType() == Material.TRIPWIRE
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getClickedBlock().getLocation(), Permission.REDSTONE, event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    // Armor Stand

    @EventHandler
    public void onEntityDamageByEntity$1(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand)) return;

        Player attacker = null;
        if (event.getDamager() instanceof Player player) attacker = player;
        else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) attacker = player;
        if (attacker == null) return;

        if (cache.isBypassing(attacker)) return;
        if (!cannotInteract(attacker, event.getEntity().getLocation(), Permission.ARMOR_STAND, event.getEntity())) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getCaught() == null || !(event.getCaught() instanceof ArmorStand)) return;

        if (cache.isBypassing(event.getPlayer())) return;
        if (!cannotInteract(event.getPlayer(), event.getCaught().getLocation(), Permission.ARMOR_STAND, event.getCaught())) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract$11(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && event.getClickedBlock().getType() == Material.ARMOR_STAND
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getClickedBlock().getLocation(), Permission.ARMOR_STAND, event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract$15(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && event.getItem() != null
            && event.getInteractionPoint() != null
            && event.getItem().getType() == Material.ARMOR_STAND
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getInteractionPoint(), Permission.ARMOR_STAND, event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getRightClicked().getLocation(), Permission.ARMOR_STAND, event.getRightClicked())) {
            event.setCancelled(true);
        }
    }

    // Composting
    @EventHandler
    public void onPlayerInteract$12(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && event.getClickedBlock().getType() == Material.COMPOSTER
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getClickedBlock().getLocation(), Permission.COMPOSTING, event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    // Beacon Access
    @EventHandler
    public void onPlayerInteract$13(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && event.getClickedBlock().getType() == Material.BEACON
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getClickedBlock().getLocation(), Permission.BEACON, event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    // Lectern
    @EventHandler
    public void onPlayerTakeLecternBook(PlayerTakeLecternBookEvent event) {
        if (!cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getLectern().getLocation(), Permission.LECTERN, event.getLectern())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace$0(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.LECTERN
            && Tag.ITEMS_LECTERN_BOOKS.isTagged(event.getItemInHand().getType())
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getBlock().getLocation(), Permission.LECTERN, event.getBlock())
        ) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        pvpGracePeriod.remove(event.getPlayer().getUniqueId());
    }

    public void onExitPvpProtection(Player player, boolean pvpOff) {
        int gracePeriod = Config.get().getLeaveNoPvpGracePeriod();
        Message message = pvpOff
            ? Messages.get().getNowLeavingPvpProtectionPvpOff()
            : Messages.get().getNowLeavingPvpProtection();
        message.send(player, Formatter.number("grace_period", gracePeriod));
        if (Config.get().getLeaveNoPvpGracePeriod() <= 0) {
            return;
        }

        pvpGracePeriod.put(player.getUniqueId(), System.currentTimeMillis() + (gracePeriod * 1000L));
    }

    // Chunk region information
    public boolean isVehicleOrAnyPassengerBanned(Player recipient, Entity vehicle, List<UUID> bannedUUIDs) {
        if (vehicle instanceof Player player && bannedUUIDs.contains(player.getUniqueId()) && !cache.isBypassing(player)) {
            Messages.get().getPlayer().getBannedWarningTitle().send(recipient != null ? recipient : player);
            return true;
        }

        List<Entity> passengers = vehicle.getPassengers();
        if (passengers.isEmpty()) {
            return false;
        }

        for (Entity passenger : passengers) {
            if (isVehicleOrAnyPassengerBanned(recipient, passenger, bannedUUIDs)) {
                return true;
            }
        }
        return false;
    }
    @EventHandler
    public void onVehicleMove(EntityMoveEvent event) {
        final Location from = event.getFrom();
        final Location to = event.getTo();

        if ((from.getBlockX() >> 4) == (to.getBlockX() >> 4) && (from.getBlockZ() >> 4) == (to.getBlockZ() >> 4)) return; // Same chunk

        List<Entity> passengers = event.getEntity().getPassengers();
        if (passengers.isEmpty()) {
            return;
        }

        boolean hasPlayer = false;
        for (Entity passenger : passengers) {
            if (passenger instanceof Player) {
                hasPlayer = true;
                break;
            }
        }

        if (!hasPlayer) {
            return;
        }

        final Town oldTown = cache.getTownByLocation(from);
        final Town newTown = cache.getTownByLocation(to);

        if (oldTown == null && newTown != null && isVehicleOrAnyPassengerBanned(null, event.getEntity(), newTown.getBannedUUIDs())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        final Location from = event.getFrom();
        final Location to = event.getTo();
        final Player player = event.getPlayer();

        if ((from.getBlockX() >> 4) == (to.getBlockX() >> 4) && (from.getBlockZ() >> 4) == (to.getBlockZ() >> 4)) return; // Same chunk

        final Town oldTown = cache.getTownByLocation(from);
        final Town newTown = cache.getTownByLocation(to);

        if (oldTown == null && newTown == null) return; // Continuing in Wilderness

        if (oldTown != null && newTown == null) { // Entering Wilderness
            Messages.get().getNowEnteringWilderness().send(player);
            if (!oldTown.getToggle(Setting.PVP) && Config.get().isAllowPvpInWilderness()) {
                Hooks.useHookOrElse(PvPManagerHook.class, hook -> {
                    if (!hook.isInCombat(player)) {
                        onExitPvpProtection(player, !hook.hasPvPEnabled(player));
                    }
                }, () -> onExitPvpProtection(player, false));
            } else if (oldTown.getToggle(Setting.PVP) && !Config.get().isAllowPvpInWilderness()) {
                Hooks.useHookOrElse(PvPManagerHook.class, hook -> {
                    if (hook.isInCombat(player)) {
                        return;
                    }

                    Message message = hook.hasPvPEnabled(player)
                        ? Messages.get().getNowEnteringPvpProtection()
                        : Messages.get().getNowEnteringPvpProtectionPvpOff();
                    message.send(player);
                }, () -> Messages.get().getNowEnteringPvpProtection().send(player));
            }
            return;
        }

        if (oldTown != newTown) { // Entering new Territory
            List<UUID> bannedUUIDs = newTown.getBannedUUIDs();
            boolean banned = isVehicleOrAnyPassengerBanned(player, player, bannedUUIDs);
            if (banned) {
                Entity vehicle = player.getVehicle();
                if (vehicle != null) {
                    Location vehicleFrom = from.clone();
                    vehicleFrom.setY(vehicle.getY());
                    player.leaveVehicle();
                    vehicle.teleport(vehicleFrom, TeleportFlag.EntityState.RETAIN_PASSENGERS);
                }
                event.setCancelled(true);
                return;
            }

            Messages.get().getNowEnteringTown().send(player, Placeholder.unparsed("town", newTown.getName()));
            if (oldTown != null && !oldTown.getToggle(Setting.PVP) && newTown.getToggle(Setting.PVP)) {
                Hooks.useHookOrElse(PvPManagerHook.class, hook -> {
                    if (!hook.isInCombat(player)) {
                        onExitPvpProtection(player, !hook.hasPvPEnabled(player));
                    }
                }, () -> onExitPvpProtection(player, false));
            } else if ((oldTown == null || oldTown.getToggle(Setting.PVP)) && !newTown.getToggle(Setting.PVP)) {
                Hooks.useHookOrElse(PvPManagerHook.class, hook -> {
                    if (hook.isInCombat(player)) {
                        return;
                    }

                    Message message = hook.hasPvPEnabled(player)
                        ? Messages.get().getNowEnteringPvpProtection()
                        : Messages.get().getNowEnteringPvpProtectionPvpOff();
                    message.send(player);
                }, () -> Messages.get().getNowEnteringPvpProtection().send(player));
            }
            return;
        }

        // Same town, new chunk
        if (isVehicleOrAnyPassengerBanned(player, player, newTown.getBannedUUIDs())) {
            if (Config.get().getTownBanExpel().isEnabled()) {
                Config.get().getTownBanExpel().expel(player);
            }
            event.setCancelled(true);
            return;
        }

        // new plot
        Plot plot = newTown.getPlot(event.getTo());
        if (plot != null) {
            Messages.get().getTown().getPlot().getEnter().send(
                player,
                Placeholder.unparsed("plot", plot.getName()),
                Placeholder.unparsed("type", plot.getType().name())
            );

            // Disable their fly if it is a PVP plot
            if (plot.getType() == PlotType.ARENA && !(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {
                player.setAllowFlight(false);
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        final Town town = cache.getTownByLocation(event.getTo());
        if (town != null && town.getBannedUUIDs().contains(event.getPlayer().getUniqueId()) && !cache.isBypassing(event.getPlayer())) {
            Messages.get().getPlayer().getBannedWarningTitle().send(event.getPlayer());
            event.setCancelled(true);
        }
    }

    // Border stuff

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (borderCheck(event.getBlock(), event.getDirection().getOppositeFace(), event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (borderCheck(event.getBlock(), event.getDirection(), event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (crossesBorder(event.getBlock(), event.getToBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode$1(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> crossesBorder(event.getEntity().getLocation().getBlock(), block));
    }

    @EventHandler
    public void onEntityExplode$2(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof WindCharge windCharge)) return;
        if (!(windCharge.getShooter() instanceof Player player)) return;

        if (!cache.isBypassing(player) && cannotInteract(player, windCharge.getLocation(), Permission.BLOCK_BREAK, Material.AIR)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode$3(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed tntPrimed)) return;

        Location location = tntPrimed.getLocation();
        Entity source = tntPrimed.getSource();

        if (source instanceof Player player) {
            if (!cache.isBypassing(player) && cannotInteract(player, location, Permission.BLOCK_BREAK, Material.AIR)) {
                event.setCancelled(true);
            }
        } else {
            // Source is null or not a player
            if (cache.getTownByLocation(location) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> crossesBorder(event.getBlock(), block));
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        if (event.getBlocks().isEmpty()) {
            return;
        }

        BlockState first = event.getBlocks().get(0);
        if (event.getEntity() instanceof Player player
            && !cache.isBypassing(player)
            && cannotInteract(player, first.getLocation(), Permission.BLOCK_PLACE, first.getBlock().getType())
        ) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        if (event.getEntity() instanceof Player player
            && !cache.isBypassing(player)
            && cannotInteract(player, event.getBlock().getLocation(), Permission.BLOCK_PLACE, event.getBlock().getType())
        ) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        List<BlockState> blocks = event.getBlocks();
        if (blocks.isEmpty()) return;
        Block first = blocks.getFirst().getBlock();
        if (blocks.stream().noneMatch(it -> crossesBorder(first, it.getBlock()))) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        final Town town = this.cache.getTownByLocation(event.getPlayer().getLocation());
        if (town == null) {
            return;
        }

        final Plot plot = town.getPlot(event.getPlayer().getLocation());
        if (plot == null) {
            return;
        }

        if (plot.getType().isCommandBlacklisted(event.getMessage())) {
            Messages.get().getPlayer().getCantDoThatCommandInThisPlotType().send(event.getPlayer(), Placeholder.unparsed("type", Message.formatEnum(plot.getType())));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity$2(EntityDamageByEntityEvent event) {
        // Figure the attacker
        Player attacker = null;
        if (event.getDamager() instanceof Player player) attacker = player;
        else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) attacker = player;
        if (attacker == null) return;
        // Figure the victim
        if (!(event.getEntity() instanceof Player victim)) return;

        // Don't proc in non-whitelisted worlds
        if (Config.get().getBlacklistedWorlds().contains(victim.getWorld().getName())) return;

        // If either player is in grace period, cancel
        Long attackerGrace = pvpGracePeriod.get(attacker.getUniqueId());
        if (attackerGrace != null && attackerGrace > System.currentTimeMillis()) {
            event.setCancelled(true);
            return;
        }
        Long victimGrace = pvpGracePeriod.get(victim.getUniqueId());
        if (victimGrace != null && victimGrace > System.currentTimeMillis()) {
            event.setCancelled(true);
            return;
        }

        ChunkPosition attackerLocation = ChunkPosition.chunkPosition(attacker.getLocation());
        ChunkPosition victimLocation = ChunkPosition.chunkPosition(victim.getLocation());
        Town attackerTown = cache.getTownByChunk(attackerLocation);
        Town victimTown = cache.getTownByChunk(victimLocation);

        // If both players are in wilderness, and we allow pvp in wilderness, allow it
        if (attackerTown == null && victimTown == null) {
            if (Config.get().isAllowPvpInWilderness()) return;

            event.setCancelled(true);
            return;
        }

        BiPredicate<Town, Location> permitsPvpHalfHalf = (town, location) -> {
            // We're expecting the other player to be in wilderness
            if (!Config.get().isAllowPvpInWilderness()) return false;

            Plot plot = town.getPlot(location);
            // If the plot was at all modified, only allow if it's an arena plot
            // Otherwise, check the town toggle
            if (plot != null && plot.getType() != PlotType.ARENA) return true;
            else return town.getToggle(Setting.PVP);
        };

        // If the attacker is in wilderness, and the victim is not, and the attacker is not banned from the victim's town
        if (attackerTown == null) {
            if (permitsPvpHalfHalf.test(victimTown, victim.getLocation()) && victimTown.checkBan(attacker).isEmpty()) return;

            event.setCancelled(true);
            return;
        }

        // If the attacker is in wilderness, and the victim is not, and the victim is not banned from the attacker's town
        if (victimTown == null) {
            if (permitsPvpHalfHalf.test(attackerTown, attacker.getLocation()) && attackerTown.checkBan(victim).isEmpty()) return;

            event.setCancelled(true);
            return;
        }

        // If both players are in a town
        Plot attackerPlot = attackerTown.getPlot(attacker.getLocation());
        Plot victimPlot = victimTown.getPlot(victim.getLocation());

        if (attackerPlot != null && attackerPlot.getType() == PlotType.ARENA
            && victimPlot != null && victimPlot.getType() == PlotType.ARENA) return;
        else if (attackerTown.getToggle(Setting.PVP) && victimTown.getToggle(Setting.PVP)) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntitySpawn(final EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof Enemy)) return;
        if (Config.get().getBlacklistedWorlds().contains(event.getLocation().getWorld().getName())) return;

        final Town town = cache.getTownByLocation(event.getLocation());
        if (town == null) return;

        final Plot plot = town.getPlot(event.getLocation());
        if (plot != null) {
            // If the plot was at all modified, only allow mobs if it's a mob farm plot
            if (plot.getType() != PlotType.MOB_FARM) {
                event.setCancelled(true);
            }
        } else if (!town.getToggle(Setting.MOBS)) {
            // Otherwise, check the town toggle
            event.setCancelled(true);
        }
    }

    // Rooted dirt
    @EventHandler
    public void onPlayerInteract$16(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && event.getClickedBlock().getType() == Material.ROOTED_DIRT
            && !cache.isBypassing(event.getPlayer())
            && cannotInteract(event.getPlayer(), event.getClickedBlock().getLocation(), Permission.BLOCK_BREAK, event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (event.getPotion().getEffects().stream().anyMatch(it -> it.getType() == PotionEffectType.INFESTED)
            && event.getEntity().getShooter() != null
            && event.getEntity().getShooter() instanceof Player player
            && !cache.isBypassing(player)
            && cannotInteract(player, event.getEntity().getLocation(), Permission.BLOCK_BREAK, event.getEntity())
        ) {
            event.setCancelled(true);
        }
    }

    private boolean borderCheck(final Block block, final BlockFace direction, final List<Block> blocks) {
        final Block testing = block.getRelative(direction);
        if (crossesBorder(block, testing)) {
            return true;
        }

        if (blocks.isEmpty()) {
            return false;
        }

        for (final Block blockTest : blocks) {
            if (crossesBorder(block, blockTest.getRelative(direction))) {
                return true;
            }
        }
        return false;
    }

    private boolean crossesBorder(final Block current, final Block target) {
        final Town currentTown = cache.getTownByLocation(current.getLocation());
        final Town targetTown = cache.getTownByLocation(target.getLocation());

        if (currentTown == null && targetTown == null) {
            return false;
        }

        return currentTown == null || targetTown == null;
    }

    /**
     * Check if a player can interact with something at a specific location
     *
     * @param player    the player to check
     * @param location   the location to check
     * @param permission the permission to check
     * @return if the player cannot interact at the specific location
     */
    private boolean cannotInteract(final Player player, final Location location, final Permission permission, Object queryObject) {
        return cannotInteract(player.getUniqueId(), location, permission, queryObject);
    }

    /**
     * Check if a player can interact with something at a specific location
     *
     * @param playerId     the player to check
     * @param location   the location to check
     * @param permission the permission to check
     * @return if the player cannot interact at the specific location
     */
    private boolean cannotInteract(final UUID playerId, final Location location, final Permission permission, Object queryObject) {
        return !OxyTownsPlugin.get().getOxyTownsApi().hasPermission(
            playerId,
            permission,
            ChunkPosition.chunkPosition(location),
            queryObject
        );
    }

    public <E extends BlockEvent & Cancellable> void handleFarmland(Player player, E event) {
        Location location = event.getBlock().getLocation();
        if (event.getBlock().getType() != Material.FARMLAND || cache.isBypassing(player)) return;
        Town town = cache.getTownByLocation(location);
        if (town == null) return;
        Plot plot = town.getPlot(location);

        if (plot != null
            && plot.getType() == PlotType.FARM
            && !plot.getAssignedMembers().contains(player.getUniqueId())
            && !town.hasPermission(player.getUniqueId(), Permission.PLOTS_MODIFY)
        ) {
            event.setCancelled(true);
        }
    }
}