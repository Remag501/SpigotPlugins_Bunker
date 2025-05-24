package me.remag501.bunker.util;

import me.remag501.bunker.Bunker;
import me.remag501.bunker.BunkerInstance;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.player.PlayerTeleportEvent;

public class NPCManager {
    public static boolean addNPC(Bunker plugin, World world, BunkerInstance bunkerInstance) {
        int npcID = bunkerInstance.getNpcId();

        // Check NPC exists
        NPC npc = CitizensAPI.getNPCRegistry().getById(npcID);
        if (npc == null) {
            plugin.getLogger().info("Could not load in npc!" + npcID);
            return false;
        }

        // Get location for npc and barrier block
        Location loc = bunkerInstance.getNpcLocation();
        loc.setWorld(world);
        Location barrierLoc = loc.add(0, -1, 0);

        // Place block beneath NPC
        Block block = world.getBlockAt(barrierLoc);
        block.setType(Material.BARRIER);

        // Add npc
        NPC clone = npc.clone();
        clone.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
        clone.spawn(loc);

        plugin.getLogger().info("Added NPC to world!");
        return true;
    }
}
