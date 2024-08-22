package me.remag501.bunker;

import me.remag501.bunker.commands.BunkerCommand;
import me.remag501.bunker.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Bunker extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        ConfigUtil config = new ConfigUtil(this, "bunkers.yml");

        getCommand("bunker").setExecutor(new BunkerCommand(this));
        getLogger().info("Bunker has been enabled!");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
