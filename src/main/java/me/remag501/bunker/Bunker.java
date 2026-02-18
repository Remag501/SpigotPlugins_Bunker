package me.remag501.bunker;

import me.remag501.bunker.commands.BunkerAdminCommand;
import me.remag501.bunker.commands.BunkerCommand;
//import me.remag501.bunker.commands.BunkerCommandOld;
import me.remag501.bunker.listeners.GeneratorBreakListener;
import me.remag501.bunker.listeners.OpenContainer;
import me.remag501.bunker.managers.BunkerCreationManager;
import me.remag501.bunker.managers.BunkerConfigManager;
import me.remag501.bunker.managers.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Bunker extends JavaPlugin {

    @Override
    public void onEnable() {

        // Create managers
        BunkerConfigManager bunkerConfigManager = new BunkerConfigManager(this);
        BunkerCreationManager bunkerCreationManager = new BunkerCreationManager(this, bunkerConfigManager);

        // Plugin startup logic
        saveDefaultConfig();
        ConfigManager config = new ConfigManager(this, "bunkers.yml");
        // Reload configuration
        BunkerCommand command = new BunkerCommand(this, bunkerConfigManager, bunkerCreationManager);
        getCommand("bunker").setExecutor(command);
        BunkerAdminCommand adminCommand = new BunkerAdminCommand(this, bunkerConfigManager,bunkerCreationManager);
        getCommand("bunkeradmin").setExecutor(adminCommand);
        getLogger().info("Bunker has been enabled!");
        // Register listeners
        getServer().getPluginManager().registerEvents(new OpenContainer(), this);
        getServer().getPluginManager().registerEvents(new GeneratorBreakListener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
