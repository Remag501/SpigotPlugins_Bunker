package me.remag501.bunker;

import me.remag501.bunker.commands.BunkerCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Bunker extends JavaPlugin {

    @Override
    public void onEnable() {

        String version = Bukkit.getVersion();

        if (version.contains("1.19")) {
            // Load features compatible with 1.19
            getLogger().info("Running on Minecraft 1.19");
        } else if (version.contains("1.20")) {
            // Load features for 1.20
            getLogger().info("Running on Minecraft 1.20");
        } else if (version.contains("1.21")) {
            // Load features for 1.21
            getLogger().info("Running on Minecraft 1.21");
        } else {
            getLogger().warning("Unsupported Minecraft version.");
            getServer().getPluginManager().disablePlugin(this);
        }

        saveConfig();
        // Plugin startup logic
        getCommand("bunker").setExecutor(new BunkerCommand());
        getLogger().info("Bunker has been enabled!");
    }

    @Override
    public void onDisable() {
        saveConfig();
        // Plugin shutdown logic
    }
}
