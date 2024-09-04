package me.remag501.bunker;

import me.remag501.bunker.commands.BunkerCommand;
import me.remag501.bunker.util.ConfigUtil;
import me.remag501.bunker.util.VisitRequest;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static me.clip.placeholderapi.PlaceholderAPIPlugin.getServerVersion;

public final class Bunker extends JavaPlugin {

    private Map<UUID, VisitRequest> pendingRequests = new ConcurrentHashMap<>();

    public Map<UUID, VisitRequest> getPendingRequests() {
        return pendingRequests;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        ConfigUtil config = new ConfigUtil(this, "bunkers.yml");
        // Reload configuration
        BunkerCommand command = new BunkerCommand(this);
        command.reload(null);
        getCommand("bunker").setExecutor(command);
        getLogger().info("Bunker has been enabled!");

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
