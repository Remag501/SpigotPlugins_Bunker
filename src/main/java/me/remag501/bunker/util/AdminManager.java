package me.remag501.bunker.util;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AdminManager {

    private Plugin plugin;
    private BunkerCreationManager bunkerCreationManager;

    public AdminManager(Plugin plugin, BunkerCreationManager bunkerCreationManager) {
        this.plugin = plugin;
        this.bunkerCreationManager = bunkerCreationManager;
    }

    public void previewBunker(Player player) {
        // Delete the existing preview world if it exists
        World previewWorld = Bukkit.getWorld("bunker_preview");
        if (previewWorld != null) {

            // Delete any npcs in the world
            NPCRegistry registry = CitizensAPI.getNPCRegistry();
            List<NPC> toRemove = new ArrayList<>();
            // First collect NPCs to delete
            for (NPC npc : registry) {
                if (npc.isSpawned() && npc.getEntity().getWorld().equals(previewWorld)) {
                    toRemove.add(npc);
                }
            }
            // Then despawn and destroy them
            for (NPC npc : toRemove) {
                npc.despawn();
                npc.destroy();
            }

            // Delete world via multiverse core
            MultiverseCore core = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
            MVWorldManager worldManager = core.getMVWorldManager();
            // Unload and delete a world
            if (worldManager.isMVWorld("bunker_preview")) {
                worldManager.unloadWorld("bunker_preview");
                worldManager.removeWorldFromConfig("bunker_preview");
                worldManager.deleteWorld("bunker_preview", true, true); // removeFromConfig, removeFromDisk
            }

            player.sendMessage(ChatColor.GRAY + "Deleted old preview world...");
        }

        // Create the new preview world asynchronously then teleport player when done
        new BukkitRunnable() {
            boolean complete = false;

            @Override
            public void run() {
                if (complete) {
                    World newWorld = Bukkit.getWorld("bunker_preview");
                    if (newWorld != null) {
                        Location spawn = newWorld.getSpawnLocation();
                        player.teleport(spawn);
                        player.sendMessage(ChatColor.GREEN + "Teleported to preview bunker.");
                    } else {
                        player.sendMessage(ChatColor.RED + "Preview bunker world not found.");
                    }
                    cancel();
                    return;
                }

                // Create the new preview world
                bunkerCreationManager.createBunkerWorld("bunker_preview");
                complete = true;
            }
        }.runTaskTimer(plugin, 0L, 0L);
    }

//    private void deleteWorldFolder(File path) throws IOException {
//        if (!path.exists()) return;
//
//        Files.walk(path.toPath())
//                .sorted(Comparator.reverseOrder())
//                .map(Path::toFile)
//                .forEach(File::delete);
//    }

}
