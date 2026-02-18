package me.remag501.bunker.service;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.remag501.bunker.core.BunkerInstance;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SchematicService {

    private final Plugin plugin;

    public SchematicService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void addSchematic(BunkerInstance bunkerInstance, String worldName) {
        new BukkitRunnable() {
            int attempts = 0;

            @Override
            public void run() {
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    this.cancel();
                    // Move to main thread for WorldEdit operations
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        for (BunkerInstance.SchematicWrapper wrapper : bunkerInstance.getSchematics()) {
                            paste(world, wrapper);
                        }
                        plugin.getLogger().info("Pasted all schematics for bunker world: " + worldName);
                    });
                } else if (++attempts >= 100) {
                    this.cancel();
                    plugin.getLogger().warning("World " + worldName + " failed to load. Paste aborted.");
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 20L);
    }

    private void paste(World world, BunkerInstance.SchematicWrapper wrapper) {
        Clipboard clipboard = loadSchematic(wrapper.file);
        if (clipboard == null) return;

        Location loc = wrapper.location.clone();
        loc.setWorld(world);

        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
            editSession.setFastMode(true);

            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()))
                    .build();

            Operations.complete(operation);
        } catch (WorldEditException e) {
            plugin.getLogger().severe("WorldEdit error while pasting: " + e.getMessage());
        }
    }

    // Helper
    private Clipboard loadSchematic(File file) {
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) return null;

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            return reader.read();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}