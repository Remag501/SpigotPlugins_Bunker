package me.remag501.bunker.service;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import me.remag501.bunker.core.BunkerInstance;
import me.remag501.bunker.util.SchematicUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SchematicService {

    private final Plugin plugin;

    public SchematicService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void addSchematic(BunkerInstance bunkerInstance, String worldName) {
        int[] attempts = {0};
        int maxAttempts = 100;

        new BukkitRunnable() {
            @Override
            public void run() {
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    this.cancel();

                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        for (BunkerInstance.SchematicWrapper wrapper : bunkerInstance.getSchematics()) {
                            Location pasteLocation = wrapper.location.clone();
                            pasteLocation.setWorld(world);

                            SchematicUtil schematic = wrapper.schematic;
                            Clipboard clipboard = schematic.loadSchematic(schematic.getFile());
                            schematic.setLocation(pasteLocation);
                            schematic.pasteSchematic(clipboard, pasteLocation);
                        }
                        plugin.getLogger().info("Pasted schematics!");
                    });

                } else if (++attempts[0] >= maxAttempts) {
                    this.cancel();
                    plugin.getLogger().warning("World " + worldName + " did not load in time. Paste aborted.");
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 20L); // every 1 second
    }


}

