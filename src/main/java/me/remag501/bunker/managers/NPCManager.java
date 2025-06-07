package me.remag501.bunker.managers;

import me.remag501.bunker.Bunker;
import me.remag501.bunker.core.BunkerInstance;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class NPCManager {
    public static boolean addNPC(Bunker plugin, World world, BunkerInstance bunkerInstance) {
        List<BunkerInstance.NPCInfo> npcs = bunkerInstance.getNpcs();
        if (npcs == null || npcs.isEmpty()) return false;

        for (BunkerInstance.NPCInfo info : npcs) {
            int npcID = info.id;
            Location loc = info.location;
            loc.setWorld(world); // ensure the world is set

            // Validate NPC ID
            NPC npc = CitizensAPI.getNPCRegistry().getById(npcID);
            if (npc == null) {
                plugin.getLogger().warning("NPC with ID " + npcID + " not found!");
                continue;
            }

            // Place a barrier block below
            Location barrierLoc = loc.clone().add(0, -1, 0);
            Block block = world.getBlockAt(barrierLoc);
            block.setType(Material.BARRIER);

            // Clone and spawn
            NPC clone = npc.clone();
            clone.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            clone.spawn(loc);
            plugin.getLogger().info("Spawned NPC clone with ID " + npcID + " at " + loc);
        }
        return true;
    }



}
