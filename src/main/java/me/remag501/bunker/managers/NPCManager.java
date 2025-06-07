package me.remag501.bunker.managers;

import me.remag501.bunker.Bunker;
import me.remag501.bunker.core.BunkerInstance;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class NPCManager {
    public static void addNPC(Bunker plugin, String worldName, BunkerInstance bunkerInstance) {
        int[] attempts = {0};
        int maxAttempts = 100;

        new BukkitRunnable() {
            @Override
            public void run() {
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    this.cancel();
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        List<BunkerInstance.NPCInfo> npcs = bunkerInstance.getNpcs();
                        if (npcs == null || npcs.isEmpty()) return;

                        for (BunkerInstance.NPCInfo info : npcs) {
                            int npcID = info.id;
                            Location loc = info.location;

                            if (loc == null) {
                                plugin.getLogger().warning("NPC location is null.");
                                continue;
                            }

                            loc.setWorld(world); // set world just in case

                            // Validate NPC ID
                            NPC npc = CitizensAPI.getNPCRegistry().getById(npcID);
                            if (npc == null) {
                                plugin.getLogger().warning("NPC with ID " + npcID + " not found!");
                                continue;
                            }

                            // Place barrier below
                            Location barrierLoc = loc.clone().add(0, -1, 0);
                            Block block = world.getBlockAt(barrierLoc);
                            block.setType(Material.BARRIER);

                            // Ensure chunk is loaded
                            if (!loc.getChunk().isLoaded()) {
                                loc.getChunk().load(true);
                            }

                            // Clone and spawn
                            NPC clone = npc.clone();
                            clone.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                            clone.spawn(loc);

                            // Bukkit teleport
                            Entity npcEntity = clone.getEntity();
                            npcEntity.teleport(loc);

                            plugin.getLogger().info("Spawned NPC clone with ID " + npcID + " at " + loc);
                        }
                    });
                } else if (++attempts[0] >= maxAttempts) {
                    plugin.getLogger().warning("World '" + worldName + "' did not load in time. NPCs not spawned.");
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 20L); // check every 1 second
    }



}
