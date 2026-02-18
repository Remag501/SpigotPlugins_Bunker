package me.remag501.bunker.managers;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.remag501.bunker.Bunker;
import me.remag501.bunker.core.BunkerInstance;
import me.remag501.bunker.service.*;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BunkerCreationManager {

    private final Bunker plugin;
    private final ConfigManager bunkerConfig;
    private final BunkerConfigManager bunkerConfigManager;
    private final GeneratorService generatorService;
    private final HologramService hologramService;
    private final NPCService npcService;
    private final SchematicService schematicService;
    private final WorldGuardService worldGuardService;

    private final Set<UUID> runningTasks = new HashSet<>();

    public BunkerCreationManager(Bunker plugin, ConfigManager bunkerConfig, BunkerConfigManager bunkerConfigManager, GeneratorService generatorService,
                                 HologramService hologramService, NPCService npcService, SchematicService schematicService, WorldGuardService worldGuardService) {
        this.plugin = plugin;
        this.bunkerConfig = bunkerConfig;
        this.bunkerConfigManager = bunkerConfigManager;
        this.generatorService = generatorService;
        this.hologramService = hologramService;
        this.npcService = npcService;
        this.schematicService = schematicService;
        this.worldGuardService = worldGuardService;
    }

    // ---------------- Bunker Assignment & Config Access ----------------

    public boolean hasBunker(String playerName) {
        return bunkerConfig.getConfig().contains(playerName.toUpperCase() + ".id");
    }
    public void reloadBunkerConfig() {
        bunkerConfig.reload(); // reloads from disk
    }

    public int getAssignedBunkers() {
        return bunkerConfig.getConfig().getInt("assignedBunkers");
    }

    public int getTotalBunkers() {
        return bunkerConfig.getConfig().getInt("totalBunkers");
    }

    public void setTotalBunkers(int total) {
        bunkerConfig.getConfig().set("totalBunkers", total);
        bunkerConfig.save();
    }

    public BunkerConfigManager getConfigManger() {
        return bunkerConfigManager;
    }

    public boolean upgradeBunker(Player player, String bunkerLevel) {
        // Update bunker config to show upgrades
        String playerName = player.getName();
        List<String> upgrades = bunkerConfig.getConfig().getStringList(playerName.toUpperCase() + ".upgrades");
        plugin.getLogger().info(upgrades.toString());
        if (!upgrades.contains(bunkerLevel)) {
            upgrades.add(bunkerLevel);
            bunkerConfig.getConfig().set(playerName.toUpperCase() + ".upgrades", upgrades);
            bunkerConfig.save();
        } else
            return false;
        // Get world and upgrade bunker
        String worldName = getWorldName(playerName);
        World world = Bukkit.getWorld(worldName);
        return upgradeBunkerWorld(world, bunkerLevel, player);
    }

    public boolean assignBunker(String playerName) {
        // Check if own bunker or if enough exists
        if (hasBunker(playerName)) return false;
        // Enough should exist
        int assigned = getAssignedBunkers();
        int total = getTotalBunkers();
//        Bukkit.getPlayer(playerName).sendMessage("reached " + assigned + " " + total);
        if (assigned >= total) return false;

        // Update the config
        bunkerConfig.getConfig().set("assignedBunkers", assigned + 1);
        bunkerConfig.getConfig().set(playerName.toUpperCase() + ".id", assigned);
        bunkerConfig.save();

        // Add generators to bunker (needs to belong to player)
        BunkerInstance bunkerInstance = bunkerConfigManager.getBunkerInstance("main");
        World world = Bukkit.getWorld(getWorldName(playerName));
        generatorService.createGenerator(Bukkit.getPlayer(playerName), world, bunkerInstance);

        return true;
    }

    public String getWorldName(String playerName) {
        return "bunker_" + bunkerConfig.getConfig().getString(playerName.toUpperCase()+".id");
    }

    // ---------------- Bunker World Creation ----------------

    public boolean addBunkers(int count, CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        if (runningTasks.contains(playerId)) {
            player.sendMessage(ChatColor.RED + "A bunker creation task is already running.");
            return true;
        }

        runningTasks.add(playerId);
        int oldTotal = getTotalBunkers();
        setTotalBunkers(oldTotal + count);

        // Process one by one to prevent WorldGuard/Citizens collisions
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i >= count) {
                    sender.sendMessage(ChatColor.GREEN + "Successfully created " + count + " bunkers.");
                    bunkerConfig.reload();
                    runningTasks.remove(playerId);
                    cancel();
                    return;
                }

                String worldName = "bunker_" + (oldTotal + i);

                // Trigger creation and content setup
                createBunkerWorld(worldName);

                i++;
            }
        }.runTaskTimer(plugin, 0L, 60L); // 3-second gap for safety

        return true;
    }

    public void createBunkerWorld(String worldName) {
        plugin.getLogger().info("Initializing creation for: " + worldName);
        BunkerInstance bunkerInstance = bunkerConfigManager.getBunkerInstance("main");

        MultiverseCore multiverseCore = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        MVWorldManager worldManager = multiverseCore.getMVWorldManager();

        // 1. Trigger Multiverse to create the world
        if (worldManager.getMVWorld(worldName) == null) {
            boolean success = worldManager.addWorld(
                    worldName,
                    World.Environment.NORMAL,
                    null,
                    WorldType.FLAT,
                    false,
                    "VoidGen"
            );
            if (!success) {
                plugin.getLogger().severe("Multiverse failed to create " + worldName);
                return;
            }
        }

        // 2. We MUST wait for the world to be loaded in Bukkit's memory
        // Before running WorldGuard or Citizens logic.
        new BukkitRunnable() {
            @Override
            public void run() {
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().warning("World " + worldName + " not yet visible to Bukkit. Retrying content load...");
                    return; // The task will repeat or we can schedule a one-off later
                }

                setupWorldContent(world, bunkerInstance);
                cancel();
            }
        }.runTaskTimer(plugin, 5L, 10L);
    }

    public void setupWorldContent(World world, BunkerInstance bunkerInstance) {
        String worldName = world.getName();
        MultiverseCore multiverseCore = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if (multiverseCore == null) return;

        MVWorldManager worldManager = multiverseCore.getMVWorldManager();
        MultiverseWorld mvWorld = worldManager.getMVWorld(world);

        // 1. Core Bukkit/MV Settings (Safe to do immediately)
        applyWorldSettings(world, mvWorld);

        // 2. Wait for the World to be "Ready"
        // We target the spawn chunk. When this completes, the world is ticked and WorldGuard
        // will have recognized the new world instance.
        world.getChunkAtAsync(world.getSpawnLocation()).thenAccept(chunk -> {

            // Return to sync thread for API interactions (WG, Citizens, etc.)
            Bukkit.getScheduler().runTask(plugin, () -> {

                // 3. WorldGuard Phase
                // Now that the world is loaded/ticked, the RegionManager is guaranteed to exist
                worldGuardService.setupBunkerFlags(world);

                // 4. Schematic Phase
                schematicService.addSchematic(bunkerInstance, worldName);

                // 5. Citizens/Hologram Phase
                npcService.addNPC(worldName, bunkerInstance);
                hologramService.addHologram(bunkerInstance, world);

                plugin.getLogger().info("Successfully initialized all systems for " + worldName);
            });
        });
    }

    private void applyWorldSettings(World world, MultiverseWorld mvWorld) {
        Location newSpawn = bunkerConfigManager.getSpawnLocation();
        if (mvWorld != null) {
            mvWorld.setAdjustSpawn(false);
            mvWorld.setSpawnLocation(newSpawn);
            mvWorld.setDifficulty(Difficulty.PEACEFUL);
        }
        world.setSpawnLocation(newSpawn);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
    }

    public boolean upgradeBunkerWorld(World world, String bunkerLevel, Player player) {
        // Get the BunkerInstance for the world
        BunkerInstance bunkerInstance = bunkerConfigManager.getBunkerInstance(bunkerLevel);
        if (bunkerInstance == null) {
            Bukkit.getLogger().warning("No bunker instance found for world: " + world.getName());
            return false;
        }

        // Add schematic
        schematicService.addSchematic(bunkerInstance, world.getName());

        // Add NPC
        npcService.addNPC(world.getName(), bunkerInstance);

        // Add generator
        generatorService.createGenerator(player, world, bunkerInstance);

        // Add hologram to world
        hologramService.addHologram(bunkerInstance, world);

        // Remove holograms from world
        hologramService.removeHolograms(bunkerInstance, world.getName());

        Bukkit.getLogger().info("Bunker in world " + world.getName() + " upgraded to level " + bunkerLevel + ".");
        return true;
    }
}
