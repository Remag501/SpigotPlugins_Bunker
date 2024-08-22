package me.remag501.bunker.util;

//import com.sk89q.worldedit.EditSession;
//import com.sk89q.worldedit.MaxChangedBlocksException;
//import com.sk89q.worldedit.CuboidClipboard;
//import com.sk89q.worldedit.schematic.MCEditSchematicFormat;
//import com.sk89q.worldedit.Vector;
//import com.sk89q.worldedit.WorldEdit;
//import com.sk89q.worldedit.WorldEditException;
//import com.sk89q.worldedit.bukkit.BukkitAdapter;
//import com.sk89q.worldedit.bukkit.BukkitWorld;
//import com.sk89q.worldedit.bukkit.WorldEditPlugin;
//import com.sk89q.worldedit.extent.clipboard.Clipboard;
//import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
//import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
//import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
//import com.sk89q.worldedit.extent.clipboard.io.MCEditSchematicReader;
//import com.sk89q.worldedit.function.operation.Operation;
//import com.sk89q.worldedit.function.operation.Operations;
//import com.sk89q.worldedit.session.ClipboardHolder;
//import com.sk89q.worldedit.util.Location;
//import com.sk89q.worldedit.world.DataException;
//import com.sk89q.worldedit.world.World;
//import org.bukkit.Bukkit;
//import org.bukkit.plugin.Plugin;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.Optional;
//
//public class Schematic {

//    private final Clipboard clipboard;
//
//    public Schematic(Clipboard clipboard) {
//        this.clipboard = clipboard;
//    }
//
//    public Clipboard getClipboard() {
//        return clipboard;
//    }
//
//    public void paste(org.bukkit.Location target) {
//        World world = BukkitAdapter.adapt(target.getWorld());
//        Location location = BukkitAdapter.adapt(target);
//
//        EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
//
//        Operation operation = new ClipboardHolder(clipboard).createPaste(session)
//                .to(location.toVector().toBlockPoint()).ignoreAirBlocks(true).build();
//
//        try {
//            Operations.complete(operation);
//
//            session.flushSession();
//        } catch (WorldEditException exception) {
//            exception.printStackTrace();
//        }
//    }
//
//    public static Optional<Schematic> load(File file) {
//        ClipboardFormat format = ClipboardFormats.findByFile(file);
//        if (format == null) {
//            return Optional.empty();
//        }
//
//        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
//            return Optional.of(new Schematic(reader.read()));
//        } catch (IOException exception) {
//            exception.printStackTrace();
//        }
//
//        return Optional.empty();
//    }

//    private File schematicFile;
//    private Location location;
//
//    public Schematic (Location location, File schematicFile) {
//        this.schematicFile = schematicFile;
//        this.location = location;
//    }
//
//    public void loadSchematic() {
//        WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
//        EditSession session = worldEditPlugin.getWorldEdit().getEditSessionFactory().getEditSession(new BukkitWorld(location.getWorld()));
//        try {
//            Clipboard clipboard = loadSchematic(schematicFile);
//            clipboard.paste(session, new Vector(location.getX(), location.getY(), location.getZ()));
//            System.out.println("Schematic pasted successfully.");
//        } catch (MaxChangedBlocksException | DataException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public Clipboard loadSchematic(File schematicFile) {
//        Clipboard clipboard = null;
//
//        // Find the schematic format
//        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
//        if (format == null) {
//            System.out.println("Unsupported schematic format.");
//            return null;
//        }
//
//        // Load the schematic into the clipboard
//        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
//            clipboard = reader.read();
//            System.out.println("Schematic loaded successfully.");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return clipboard;
//    }
//
//}

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
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

    public class Schematic {

        private final File schematicFile;
        private final org.bukkit.Location bukkitLocation;

        public Schematic(org.bukkit.Location bukkitLocation, File schematicFile) {
            this.schematicFile = schematicFile;
            this.bukkitLocation = bukkitLocation;
        }

        public void loadAndPasteSchematic() {
            Clipboard clipboard = loadSchematic(schematicFile);

            if (clipboard != null) {
                pasteClipboard(bukkitLocation.getWorld(), clipboard, bukkitLocation);
                System.out.println("Schematic pasted successfully.");
            } else {
                System.out.println("Failed to load the schematic.");
            }
        }

        private Clipboard loadSchematic(File schematicFile) {
            Clipboard clipboard = null;

            // Find the schematic format
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                System.out.println("Unsupported schematic format.");
                return null;
            }

            // Load the schematic into the clipboard
            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                clipboard = reader.read();
                System.out.println("Schematic loaded successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }

            return clipboard;
        }

        private void pasteClipboard(org.bukkit.World bukkitWorld, Clipboard clipboard, org.bukkit.Location bukkitLocation) {
            // Convert the Bukkit world and location to WorldEdit's equivalents
            World world = BukkitAdapter.adapt(bukkitWorld);
            Location pasteLocation = BukkitAdapter.adapt(bukkitLocation);

            // Create an EditSession to perform operations
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                // Create a ClipboardHolder for pasting the schematic
                ClipboardHolder holder = new ClipboardHolder(clipboard);

                Vector vector = new Vector(bukkitLocation.getBlockX(), bukkitLocation.getBlockY(), bukkitLocation.getBlockZ());

                // Set up the paste operation
                Operation operation = holder
                        .createPaste(editSession)
                        .to(vector)
                        .ignoreAirBlocks(true)
                        .build();

                // Complete the operation
                Operations.complete(operation);

                // Flush the session to apply the changes
                editSession.flushSession();
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        }
    }

