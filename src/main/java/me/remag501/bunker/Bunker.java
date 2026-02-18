package me.remag501.bunker;

import com.sk89q.worldguard.WorldGuard;
import me.remag501.bgscore.api.BGSApi;
import me.remag501.bgscore.api.command.CommandService;
import me.remag501.bgscore.api.event.EventService;
import me.remag501.bgscore.api.task.TaskService;
import me.remag501.bunker.commands.BunkerAdminCommand;
import me.remag501.bunker.commands.BunkerCommand;
//import me.remag501.bunker.commands.BunkerCommandOld;
import me.remag501.bunker.listeners.GeneratorBreakListener;
import me.remag501.bunker.listeners.OpenContainer;
import me.remag501.bunker.managers.AdminManager;
import me.remag501.bunker.managers.BunkerCreationManager;
import me.remag501.bunker.managers.BunkerConfigManager;
import me.remag501.bunker.managers.ConfigManager;
import me.remag501.bunker.service.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class Bunker extends JavaPlugin {

    @Override
    public void onEnable() {

        // Plugin startup logic
        saveDefaultConfig();
        ConfigManager configManager = new ConfigManager(this, "bunkers.yml");
        BunkerConfigManager bunkerConfigManager = new BunkerConfigManager(this);

        // Get services from BGS Core API
        EventService eventService = BGSApi.events();
        TaskService taskService = BGSApi.tasks();
        CommandService commandService = BGSApi.commands();

        // Create services
        HologramService hologramService = new HologramService();
        GeneratorService generatorService = new GeneratorService(this);
        NPCService npcService = new NPCService(this);
        SchematicService schematicService = new SchematicService(this);
        WorldGuardService worldGuardService = new WorldGuardService(this);

        // Create managers
        BunkerCreationManager bunkerCreationManager = new BunkerCreationManager(this, configManager,
                bunkerConfigManager, generatorService, hologramService, npcService, schematicService, worldGuardService);
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
