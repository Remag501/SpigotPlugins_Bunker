package me.remag501.bunker.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class ConfigManager {
    private File file;
    private FileConfiguration config;

    public ConfigManager(Plugin plugin, String path) {
        this.file = new File(plugin.getDataFolder(), path);

        // If the file does not exist, save the default resource
        if (!file.exists()) {
            plugin.saveResource(path, false);
        }

        // Load the configuration from the file
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public ConfigManager(String path) {
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

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }
    
    
    public File getFile() {
        return this.file;
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

}
