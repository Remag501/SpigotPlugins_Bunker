package me.remag501.bunker;

import me.remag501.bunker.commands.BunkerAdminCommand;
import me.remag501.bunker.commands.BunkerCommand;
//import me.remag501.bunker.commands.BunkerCommandOld;
import me.remag501.bunker.listeners.GeneratorBreakListener;
import me.remag501.bunker.listeners.OpenContainer;
import me.remag501.bunker.managers.BunkerCreationManager;
import me.remag501.bunker.managers.ConfigManager;
import me.remag501.bunker.util.ConfigUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Bunker extends JavaPlugin {

    @Override
    public void onEnable() {

        // Create managers
        ConfigManager configManager = new ConfigManager(this);
        BunkerCreationManager bunkerCreationManager = new BunkerCreationManager(this, configManager);

        // Plugin startup logic
        saveDefaultConfig();
        ConfigUtil config = new ConfigUtil(this, "bunkers.yml");
        // Reload configuration
        BunkerCommand command = new BunkerCommand(this, configManager, bunkerCreationManager);
        getCommand("bunker").setExecutor(command);
        BunkerAdminCommand adminCommand = new BunkerAdminCommand(this, configManager,bunkerCreationManager);
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

//    public org.bukkit.WorldType getVoidWorldType() {
//        Object voidWorldType = null;
//        // Get world type and store it as class variable
//        try {
//            // Get the NMS WorldType class
//            Class<?> worldTypeClass = Class.forName("net.minecraft.server." + getServerVersion() + ".WorldType");
//            // Get the WorldType for the void world
//            Method getByNameMethod = worldTypeClass.getMethod("getByName", String.class);
//            voidWorldType = getByNameMethod.invoke(null, "void");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return voidWorldType;
//    }

}
