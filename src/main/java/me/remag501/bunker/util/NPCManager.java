package me.remag501.bunker.util;

import me.remag501.bunker.Bunker;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.player.PlayerTeleportEvent;

public class NPCManager {
    public static boolean addNPC(Bunker plugin, World world, ConfigManager configManager) {
        int npcID = (int) configManager.getDouble("npcId");
        double npcX = configManager.getDouble("npcX");
        double npcY = configManager.getDouble("npcY");
        double npcZ = configManager.getDouble("npcZ");
        float npcYaw = (float) configManager.getDouble("npcYaw");
        float npcPitch = (float) configManager.getDouble("npcPitch");

        // Check NPC exists
        NPC npc = CitizensAPI.getNPCRegistry().getById(npcID);
        if (npc == null) return false;

        // Get location for npc and barrier block
        Location loc = new Location(world, npcX, npcY, npcZ, npcYaw, npcPitch);
        Location barrierLoc = new Location(world, npcX, npcY-1, npcZ, npcYaw, npcPitch);

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
