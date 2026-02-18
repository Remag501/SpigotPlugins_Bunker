package me.remag501.bunker.managers;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.remag501.bunker.Bunker;
import me.remag501.bunker.core.BunkerInstance;
import me.remag501.bunker.util.ConfigUtil;
import me.remag501.bunker.util.SchematicUtil;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BunkerCreationManager {
    private final Bunker plugin;
    private final Set<UUID> runningTasks = new HashSet<>();
    private final ConfigManager configManager;
    private final ConfigUtil bunkerConfig;

    public BunkerCreationManager(Bunker plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.bunkerConfig = new ConfigUtil(plugin, "bunkers.yml");
    }

    // ---------------- Bunker Assignment & Config Access ----------------

    public boolean hasBunker(String playerName) {
        return bunkerConfig.getConfig().contains(playerName.toUpperCase() + ".id");
    }

    public void reloadBunkerConfig() {
        bunkerConfig.reload(); // reloads from disk
    }

    public boolean hasBunkerUpgrade(String playerName, String level) {
        List<String> upgrades = bunkerConfig.getConfig().getStringList(playerName + ".upgrades");
        return upgrades.contains(level);
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

    public ConfigManager getConfigManger() {
        return configManager;
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
        BunkerInstance bunkerInstance = configManager.getBunkerInstance("main");
        World world = Bukkit.getWorld(getWorldName(playerName));
        GeneratorManager.createGenerator(Bukkit.getPlayer(playerName), world, bunkerInstance);

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
        BunkerInstance bunkerInstance = configManager.getBunkerInstance("main");

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
                setupWorldGuardFlags(world);

                // 4. Schematic Phase
                SchematicManager.addSchematic(plugin, bunkerInstance, worldName);

                // 5. Citizens/Hologram Phase
                NPCManager.addNPC(plugin, worldName, bunkerInstance);
                HologramManager.addHologram(bunkerInstance, world);

                plugin.getLogger().info("Successfully initialized all systems for " + worldName);
            });
        });
    }

    private void applyWorldSettings(World world, MultiverseWorld mvWorld) {
        Location newSpawn = ConfigManager.getSpawnLocation();
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


    private void setupWorldGuardFlags(World world) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
            if (regionManager == null) return;

            var globalRegion = regionManager.getRegion("__global__");
            if (globalRegion == null) {
                // Global region doesn't exist yet due to world load, manually adding it
                globalRegion = new GlobalProtectedRegion("__global__");
                regionManager.addRegion(globalRegion);
            }

            plugin.getLogger().info("Loading in world guard flags");

            StateFlag iceFlag = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("ice-melt");
            StateFlag enderFlag = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("enderpearl");
            StateFlag breakBlockFlag = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("block-break");
            StateFlag placeBlockFlag = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("block-place");
            StateFlag pvpFlag = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("pvp");
            StateFlag miFlag = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("mi-weapons");
            StateFlag mmoFlag = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("mmo-abilities");
            StateFlag fallDmgFlag = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("fall-damage");
            StateFlag lavaFlowFlag = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("lava-flow");
            StateFlag lavaFireFlag = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("lava-fire");
            StateFlag invincibleFlag = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("invincible");
//            StateFlag healAmountFlag = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("heal-amount");
//            StateFlag healDelayFlag = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("heal-delay");


            if (iceFlag != null) globalRegion.setFlag(iceFlag, StateFlag.State.DENY);
            if (enderFlag != null) globalRegion.setFlag(enderFlag, StateFlag.State.DENY);
            if (breakBlockFlag != null) globalRegion.setFlag(breakBlockFlag, StateFlag.State.DENY);
            if (placeBlockFlag != null) globalRegion.setFlag(placeBlockFlag, StateFlag.State.DENY);
            if (pvpFlag != null) globalRegion.setFlag(pvpFlag, StateFlag.State.DENY);
            if (miFlag != null) globalRegion.setFlag(miFlag, StateFlag.State.DENY);
            if (mmoFlag != null) globalRegion.setFlag(mmoFlag, StateFlag.State.DENY);
            if (fallDmgFlag != null) globalRegion.setFlag(fallDmgFlag, StateFlag.State.DENY);
            if (lavaFlowFlag != null) globalRegion.setFlag(lavaFlowFlag, StateFlag.State.DENY);
            if (lavaFireFlag != null) globalRegion.setFlag(lavaFireFlag, StateFlag.State.DENY);
            if (invincibleFlag != null) globalRegion.setFlag(invincibleFlag, StateFlag.State.ALLOW);
//            if (healAmountFlag != null) globalRegion.setFlag(healAmountFlag, StateFlag.State.valueOf(""));
//            if (healDelayFlag != null) globalRegion.setFlag(healDelayFlag, StateFlag.State.DENY);


        } catch (Exception e) {
            plugin.getLogger().warning("WG Flags failed for " + world.getName());
        }
    }

    public boolean upgradeBunkerWorld(World world, String bunkerLevel, Player player) {
        // Get the BunkerInstance for the world
        BunkerInstance bunkerInstance = configManager.getBunkerInstance(bunkerLevel);
        if (bunkerInstance == null) {
            Bukkit.getLogger().warning("No bunker instance found for world: " + world.getName());
            return false;
        }

        // Add schematic
        SchematicManager.addSchematic(plugin, bunkerInstance, world.getName());

        // Add NPC
        NPCManager.addNPC(plugin, world.getName(), bunkerInstance);

        // Add generator
        GeneratorManager.createGenerator(player, world, bunkerInstance);

        // Add hologram to world
        HologramManager.addHologram(bunkerInstance, world);

        // Remove holograms from world
        HologramManager.removeHolograms(bunkerInstance, world.getName());

        Bukkit.getLogger().info("Bunker in world " + world.getName() + " upgraded to level " + bunkerLevel + ".");
        return true;
    }
}
