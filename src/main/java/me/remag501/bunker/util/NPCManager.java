package me.remag501.bunker.util;

import me.remag501.bunker.Bunker;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.player.PlayerTeleportEvent;

public class NPCManager {
    public static boolean addNPC(Bunker plugin, World world, ConfigManager configManager) {
        int npcID = (int) configManager.getDouble("npcId");
        double npcX = configManager.getDouble("npcX");
        double npcY = configManager.getDouble("npcY");
        double npcZ = configManager.getDouble("npcZ");
        float npcYaw = (float) configManager.getDouble("npcYaw");
        float npcPitch = (float) configManager.getDouble("npcPitch");

        NPC npc = CitizensAPI.getNPCRegistry().getById(npcID);
        if (npc == null) return false;

        NPC clone = npc.clone();
        Location loc = new Location(world, npcX, npcY, npcZ, npcYaw, npcPitch);
        clone.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
        clone.spawn(loc);

        plugin.getLogger().info("Added NPC to world!");
        return true;
    }
}
