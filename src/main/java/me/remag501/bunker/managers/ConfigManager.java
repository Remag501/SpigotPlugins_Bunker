package me.remag501.bunker.managers;

import me.remag501.bunker.Bunker;
import me.remag501.bunker.core.BunkerInstance;
import me.remag501.bunker.util.SchematicUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final Bunker plugin;
    private static Map<String, String> messages = new HashMap<>();
    private static Map<String, Double> doubles = new HashMap<>();
    private static final Map<String, BunkerInstance> bunkerInstances = new HashMap<>();

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

        // Load bunker instances
        for (String bunkerKey : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(bunkerKey);
            if (section == null) continue;

            String schemName = section.getString("schematicName", "bunker.schem");
            // Save schematic for bunker level
            File schematicFile = new File(plugin.getDataFolder(), "schematics/" + schemName);
            if (!schematicFile.exists()) {
                plugin.getLogger().warning("Schematic not found for " + bunkerKey + ": " + schematicFile.getName());
                continue;
            }

            SchematicUtil schematic = new SchematicUtil(schematicFile, plugin);
            String generatorType = section.getString("generatorType", null);
            // Get locations for bunker level with helper function
            Location schematicLoc = getLocationFromSection(section.getConfigurationSection("schematicCoords"), null);
            Location spawnLoc = getLocationFromSection(section.getConfigurationSection("spawnCoords"), null);
            Location npcLoc = getLocationFromSection(section.getConfigurationSection("npcCoords"), null);
            Location generatorLoc = getLocationFromSection(section.getConfigurationSection("generatorCoords"), null);
            int npcId = section.getInt("npcId", -1);
            int generatorLevel = section.getInt("generatorLevel", 1);
            // Save to map of bunker instances
            BunkerInstance instance = new BunkerInstance(bunkerKey, schematicLoc, spawnLoc, npcLoc, schematic, generatorLoc, generatorType, npcId, generatorLevel);
            bunkerInstances.put(bunkerKey, instance);
        }
    }

    private Location getLocationFromSection(ConfigurationSection sec, org.bukkit.World world) {
        if (sec == null) return null;
        double x = sec.getDouble("x");
        double y = sec.getDouble("y");
        double z = sec.getDouble("z");
        float yaw = (float) sec.getDouble("yaw", 0f);
        float pitch = (float) sec.getDouble("pitch", 0f);
        return new Location(world, x, y, z, yaw, pitch);
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "");
    }

    public double getDouble(String key) {
        return doubles.getOrDefault(key, 0.0);
    }

    public BunkerInstance getBunkerInstance(String name) {
        return bunkerInstances.get(name);
    }
}

