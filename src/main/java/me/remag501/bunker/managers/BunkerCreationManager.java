package me.remag501.bunker.managers;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
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
        Bukkit.getPlayer(playerName).sendMessage("reached " + assigned + " " + total);
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
            player.sendMessage("The bunker creation is still in progress. Please wait.");
            return true;
        }

        runningTasks.add(playerId);

        int oldTotal = getTotalBunkers();
        setTotalBunkers(oldTotal + count);

        // Create bunkers on main thread with delay in between each creation
        int delayBetweenWorlds = 40; // 2 seconds per world
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (i >= count) {
                    sender.sendMessage("Created " + count + " bunkers.");
                    runningTasks.remove(playerId);
                    cancel();
                    return;
                }
                String worldName = "bunker_" + (oldTotal + i);
                createBunkerWorld(worldName);
                i++;
            }
        }.runTaskTimer(plugin, 0L, delayBetweenWorlds);

        return true;
    }

    public void createBunkerWorld(String worldName) {
        plugin.getLogger().info("Creating bunker world: " + worldName);

        BunkerInstance bunkerInstance = configManager.getBunkerInstance("main");

        Plugin multiversePlugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        MultiverseCore multiverseCore = (MultiverseCore) multiversePlugin;
        MVWorldManager worldManager = multiverseCore.getMVWorldManager();

        if (worldManager.getMVWorld(worldName) == null) {
            worldManager.addWorld(
                    worldName,
                    World.Environment.NORMAL,
                    null,
                    WorldType.FLAT,
                    false,
                    "VoidGen"
            );
            plugin.getLogger().info("World " + worldName + " created successfully.");
        } else {
            plugin.getLogger().info("World " + worldName + " already exists.");
            return;
        }

        // Set world attributes from config
        Location newSpawn = ConfigManager.getSpawnLocation();
        World world = Bukkit.getWorld(worldName);
        MultiverseWorld mvWorld = worldManager.getMVWorld(world);
        mvWorld.setAdjustSpawn(false);
        mvWorld.setSpawnLocation(newSpawn);
        world.setSpawnLocation(newSpawn);
        mvWorld.setDifficulty(Difficulty.PEACEFUL);
        mvWorld.setGameMode(GameMode.ADVENTURE);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);

        // Paste schematic
        SchematicManager.addSchematic(plugin, bunkerInstance, worldName);

        // Check world spawn was set correctly
        Location spawnLoc = ConfigManager.getSpawnLocation();
        if (!(world.getSpawnLocation().getX() == spawnLoc.getX() && world.getSpawnLocation().getY() == spawnLoc.getY() && world.getSpawnLocation().getZ() == spawnLoc.getZ())) {
            plugin.getLogger().info("Failed to set spawn location for world " + worldName + ". Check configuration.");
        } else
            plugin.getLogger().info("World spawn set to " + newSpawn.toString());

        // Add NPC sync since citizens requires it
        NPCManager.addNPC(plugin, worldName, bunkerInstance);

        // Add hologram to world
        HologramManager.addHologram(bunkerInstance, world);

    }

    public boolean upgradeBunkerWorld(World world, String bunkerLevel, Player player) {
        // Get the BunkerInstance for the world
        BunkerInstance bunkerInstance = configManager.getBunkerInstance(bunkerLevel);
        if (bunkerInstance == null) {
            Bukkit.getLogger().warning("No bunker instance found for world: " + world.getName());
            return false;
        }

        // Get the paste location and paste the upgraded schematic
//        Location pasteLocation = bunkerInstance.getSchematicLocation();
//        pasteLocation.setWorld(world);
//        SchematicUtil schematic = bunkerInstance.getSchematic();
//        Clipboard clipboard = schematic.loadSchematic(schematic.getFile());
//        schematic.setLocation(pasteLocation);
//        schematic.pasteSchematic(clipboard, pasteLocation);
        SchematicManager.addSchematic(plugin, bunkerInstance, world.getName());

        // Add NPC
        NPCManager.addNPC(plugin, world.getName(), bunkerInstance);

        // Add generator
        GeneratorManager.createGenerator(player, world, bunkerInstance);

        // Add hologram to world
        HologramManager.addHologram(bunkerInstance, world);

        Bukkit.getLogger().info("Bunker in world " + world.getName() + " upgraded to level " + bunkerLevel + ".");
        return true;
    }
}
