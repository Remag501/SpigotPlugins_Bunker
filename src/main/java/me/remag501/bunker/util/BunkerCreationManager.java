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

import java.util.HashSet;
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
        return bunkerConfig.getConfig().contains(playerName.toUpperCase());
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

    public boolean assignBunker(String playerName) {
        if (hasBunker(playerName)) return false;

        int assigned = getAssignedBunkers();
        int total = getTotalBunkers();
        if (assigned >= total) return false;

        bunkerConfig.getConfig().set("assignedBunkers", assigned + 1);
        bunkerConfig.getConfig().set(playerName.toUpperCase(), assigned);
        bunkerConfig.save();
        return true;
    }

    public String getWorldName(String playerName) {
        return "bunker_" + bunkerConfig.getConfig().getString(playerName.toUpperCase());
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

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            for (int i = 0; i < count; i++) {
                String worldName = "bunker_" + (oldTotal + i);
                createBunkerWorld(worldName);
            }

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                sender.sendMessage("Created " + count + " bunkers.");
                runningTasks.remove(playerId);
            });
        });

        return true;
    }

    private void createBunkerWorld(String worldName) {
        plugin.getLogger().info("Creating bunker world: " + worldName);

        int x = (int) configManager.getDouble("x");
        int y = (int) configManager.getDouble("y");
        int z = (int) configManager.getDouble("z");
        double spawnX = configManager.getDouble("spawnX");
        double spawnY = configManager.getDouble("spawnY");
        double spawnZ = configManager.getDouble("spawnZ");
        float yaw = (float) configManager.getDouble("yaw");
        float pitch = (float) configManager.getDouble("pitch");

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

        Location pasteLocation = new Location(Bukkit.getWorld(worldName), x, y, z);
        Schematic schematic = configManager.getSchematic();
        Clipboard clipboard = schematic.loadSchematic(schematic.getFile());
        schematic.setLocation(pasteLocation);
        schematic.pasteSchematic(clipboard, pasteLocation);

        Location newSpawn = new Location(Bukkit.getWorld(worldName), spawnX, spawnY, spawnZ, yaw, pitch);
        World world = Bukkit.getWorld(worldName);

        MultiverseWorld mvWorld = worldManager.getMVWorld(world);
        mvWorld.setAdjustSpawn(false);
        mvWorld.setSpawnLocation(newSpawn);
        world.setSpawnLocation(newSpawn);
        mvWorld.setDifficulty(Difficulty.PEACEFUL);
        mvWorld.setGameMode(GameMode.ADVENTURE);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);

        if (!(world.getSpawnLocation().getX() == spawnX && world.getSpawnLocation().getY() == spawnY && world.getSpawnLocation().getZ() == spawnZ)) {
            plugin.getLogger().info("Failed to set spawn location for world " + worldName + ". Check configuration.");
        }

        plugin.getLogger().info("World spawn set to " + newSpawn.toString());

        NPCManager.addNPC(plugin, world, configManager);
    }
}
