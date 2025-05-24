package me.remag501.bunker.util;

import me.remag501.bunker.Bunker;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final Bunker plugin;
    private Map<String, String> messages = new HashMap<>();
    private Map<String, Double> doubles = new HashMap<>();
    Schematic schematic = null;

    public ConfigManager(Bunker plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // Load messages
        messages.put("argCommandUsage", config.getString("argCommandUsage"));
        messages.put("visitCommandUsage", config.getString("visitCommandUsage"));
        messages.put("noBunker", config.getString("noBunker"));
        messages.put("outOfBunkers", config.getString("outOfBunkers"));
        messages.put("alreadyPurchased", config.getString("alreadyPurchased"));
        messages.put("bunkerPurchased", config.getString("bunkerPurchased"));
        messages.put("playerNotExist", config.getString("playerNotExist"));
        messages.put("homeMsg", config.getString("homeMsg"));
        messages.put("pendingVisitRequest", config.getString("pendingVisitRequest"));
        messages.put("requestMsg", config.getString("requestMsg"));
        messages.put("acceptRequest", config.getString("acceptRequest"));
        messages.put("declineRequest", config.getString("declineRequest"));
        messages.put("allowVisit", config.getString("allowVisit"));
        messages.put("declineVisit", config.getString("declineVisit"));

        // Load doubles
        doubles.put("x", config.getDouble("x"));
        doubles.put("y", config.getDouble("y"));
        doubles.put("z", config.getDouble("z"));
        doubles.put("spawnX", config.getDouble("spawnX"));
        doubles.put("spawnY", config.getDouble("spawnY"));
        doubles.put("spawnZ", config.getDouble("spawnZ"));
        doubles.put("yaw", config.getDouble("yaw"));
        doubles.put("pitch", config.getDouble("pitch"));
        doubles.put("npcId", config.getDouble("npcId"));
        doubles.put("npcX", config.getDouble("npcCoords.x"));
        doubles.put("npcY", config.getDouble("npcCoords.y"));
        doubles.put("npcZ", config.getDouble("npcCoords.z"));
        doubles.put("npcYaw", config.getDouble("npcCoords.yaw"));
        doubles.put("npcPitch", config.getDouble("npcCoords.pitch"));

        // Check if schematic file exists
        File schematicFile = new File(plugin.getDataFolder(), "schematics/bunker.schem");
        if (!schematicFile.exists()) {
                plugin.getLogger().info("No schematic found. Make sure you named it correctly and its placed in schematics/bunker.schem");
                return;
        }
        // Reload schematic
        schematic = new Schematic(schematicFile, plugin);
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "");
    }

    public double getDouble(String key) {
        return doubles.getOrDefault(key, 0.0);
    }

    public Schematic getSchematic() {
        return schematic;
    }
}

