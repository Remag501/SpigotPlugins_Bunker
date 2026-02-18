package me.remag501.bunker;

import me.remag501.bunker.commands.BunkerAdminCommand;
import me.remag501.bunker.commands.BunkerCommand;
//import me.remag501.bunker.commands.BunkerCommandOld;
import me.remag501.bunker.listeners.GeneratorBreakListener;
import me.remag501.bunker.listeners.OpenContainer;
import me.remag501.bunker.managers.AdminManager;
import me.remag501.bunker.managers.BunkerCreationManager;
import me.remag501.bunker.managers.BunkerConfigManager;
import me.remag501.bunker.managers.ConfigManager;
import me.remag501.bunker.service.GeneratorService;
import me.remag501.bunker.service.HologramService;
import me.remag501.bunker.service.NPCService;
import me.remag501.bunker.service.SchematicService;
import org.bukkit.plugin.java.JavaPlugin;

public final class Bunker extends JavaPlugin {

    @Override
    public void onEnable() {

        // Plugin startup logic
        saveDefaultConfig();
        ConfigManager configManager = new ConfigManager(this, "bunkers.yml");

        // Create services
        HologramService hologramService = new HologramService();
        GeneratorService generatorService = new GeneratorService(this);
        NPCService npcService = new NPCService(this);
        SchematicService schematicService = new SchematicService(this);

        // Create managers
        BunkerConfigManager bunkerConfigManager = new BunkerConfigManager(this);
        BunkerCreationManager bunkerCreationManager = new BunkerCreationManager(this, configManager,
                bunkerConfigManager, generatorService, hologramService, npcService, schematicService);
        AdminManager adminManager = new AdminManager(this, bunkerCreationManager, hologramService, generatorService);

        // Setup commands
        BunkerCommand command = new BunkerCommand(this, bunkerConfigManager, bunkerCreationManager);
        getCommand("bunker").setExecutor(command);
        BunkerAdminCommand adminCommand = new BunkerAdminCommand(bunkerConfigManager, bunkerCreationManager, adminManager);
        getCommand("bunkeradmin").setExecutor(adminCommand);

        // Register listeners
        getServer().getPluginManager().registerEvents(new OpenContainer(), this);
        getServer().getPluginManager().registerEvents(new GeneratorBreakListener(), this);

        // Send startup message
        getLogger().info("Bunker has been enabled!");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
