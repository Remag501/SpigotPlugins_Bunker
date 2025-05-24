package me.remag501.bunker.util;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Schematic {

    private File file;
    private Location location;
    private Plugin plugin;

    public Schematic(File file, Location location, Plugin plugin) {
        this.file = file;
        this.location = location;
        this.plugin = plugin;
    }

    public Schematic(File file, Plugin plugin) {
        this.file = file;
        this.plugin = plugin;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void loadAndPasteSchematic() {
        Clipboard clipboard = loadSchematic(file);
        pasteSchematic(clipboard, location);
    }

    public Clipboard loadSchematic(File file) {
        Clipboard clipboard;

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return clipboard;
    }

    public void pasteSchematic(Clipboard clipboard, Location location) {
        World world = BukkitAdapter.adapt(location.getWorld());
        plugin.getLogger().info("Pasting schematic");

        // Run the entire paste operation on the main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                editSession.setFastMode(true);  // Enable fast mode for faster pasting

                // Build the paste operation
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                        .build();

                // Complete the operation
                Operations.complete(operation);

                plugin.getLogger().info("Pasting schematic completed successfully!");
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        });
    }

    public File getFile() {
        return this.file;
    }
}