package me.remag501.bunker.util;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import me.remag501.bunker.Bunker;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.ObjectInputFilter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BunkerCreationManager {
    private final Bunker plugin;
    private final Set<UUID> runningTasks = new HashSet<>();
    private final BunkerManager bunkerManager;
    private final ConfigManager configManager;

    public BunkerCreationManager(Bunker plugin, BunkerManager bunkerManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.bunkerManager = bunkerManager;
        this.configManager = configManager;
    }

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

        // Update total bunkers count in bunkers.yml via BunkerManager
        int oldTotal = bunkerManager.getTotalBunkers();
        bunkerManager.setTotalBunkers(oldTotal + count);

        // Run bunker world creation async to avoid server lag
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            for (int i = 0; i < count; i++) {
                String worldName = "bunker_" + (oldTotal + i);
                createBunkerWorld(worldName);
            }
            // Back to main thread to notify player
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                sender.sendMessage("Created " + count + " bunkers.");
                runningTasks.remove(playerId);
            });
        });

        return true;
    }

    // Dummy placeholder, replace with your existing method to create bunker worlds
    private void createBunkerWorld(String worldName) {
        // Your logic to create/load the bunker world, e.g., clone schematics, etc.
        plugin.getLogger().info("Creating bunker world: " + worldName);
        // Get config message strings
        int x = (int) configManager.getDouble("x");
        int y = (int) configManager.getDouble("y");
        int z = (int) configManager.getDouble("z");
        double spawnX = configManager.getDouble("spawnX");
        double spawnY = configManager.getDouble("spawnY");
        double spawnZ = configManager.getDouble("spawnZ");
        float yaw = (float) configManager.getDouble("yaw");
        float pitch = (float) configManager.getDouble("pitch");
        // Check if Multiverse-Core is installed
        Plugin multiversePlugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        MultiverseCore multiverseCore = (MultiverseCore) multiversePlugin;
        MVWorldManager worldManager = multiverseCore.getMVWorldManager();
        // Create the void world
        if (worldManager.getMVWorld(worldName) == null) {
            worldManager.addWorld(
                    worldName, // name of world
                    World.Environment.NORMAL, // enviornment
                    null, // seed
                    WorldType.FLAT, // World Type
                    false, // Generate structures
                    "VoidGen"); // Custom generator
            plugin.getLogger().info("World " + worldName + " created successfully.");
        } else {
            plugin.getLogger().info("World " + worldName + " already exists.");
            return;
        }

        // Add schematic to empty world via multithreading
        Location pasteLocation = new Location(Bukkit.getWorld(worldName), x, y, z);
        Schematic schematic = configManager.getSchematic();
        Clipboard clipboard = schematic.loadSchematic(schematic.getFile());
        schematic.setLocation(pasteLocation);
        schematic.pasteSchematic(clipboard, pasteLocation);

        // Set spawn location for new world
        Location newSpawn  = new Location(Bukkit.getWorld(worldName), spawnX, spawnY, spawnZ, yaw, pitch);
        World world = Bukkit.getWorld(worldName);

        // Disable Multiverse-Core's safe spawn enforcement
        MultiverseWorld mvWorld = worldManager.getMVWorld(world);
        mvWorld.setAdjustSpawn(false); // Disable safe teleport for this world
        mvWorld.setSpawnLocation(newSpawn); // multiverse world spawn
        world.setSpawnLocation(newSpawn); // Bukkit world spawn, wont set server spawn unless in main world
        mvWorld.setDifficulty(Difficulty.PEACEFUL); // Set difficulty to peaceful
        mvWorld.setGameMode(GameMode.ADVENTURE); // Set gamemode to adventure
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false); // Turns off mob spawning
        if (!(world.getSpawnLocation().getX() == spawnX && world.getSpawnLocation().getY() == spawnY && world.getSpawnLocation().getZ() == spawnZ))
            plugin.getLogger().info("Failed to set spawn location for world " + worldName + ". Check your configurtion.yml to adjust coordinates and make sure there are no obstructions, or it is not on air.");
        plugin.getLogger().info("World spawn set to " + newSpawn.toString());
        // Add npc to the bunker
        NPCManager.addNPC(plugin, world, configManager);
    }
}
