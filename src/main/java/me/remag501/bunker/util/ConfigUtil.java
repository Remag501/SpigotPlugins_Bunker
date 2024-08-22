package me.remag501.bunker.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class ConfigUtil {
    private File file;
    private FileConfiguration config;

    public ConfigUtil(Plugin plugin, String path) {
        this.file = new File(plugin.getDataFolder(), path);

        // If the file does not exist, save the default resource
        if (!file.exists()) {
            plugin.saveResource(path, false);
        }

        // Load the configuration from the file
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public ConfigUtil(String path) {
        this.file = new File(path);
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public boolean save() {
        try {
            this.config.save(this.file);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
    public File getFile() {
        return this.file;
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

}
