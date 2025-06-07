package me.remag501.bunker.managers;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.remag501.bunker.core.BunkerInstance;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

public class HologramManager {

    public static void addHologram(BunkerInstance bunkerInstance, World world) {
        List<BunkerInstance.HologramInfo> holograms = bunkerInstance.getHolograms();
        if (holograms == null || holograms.isEmpty()) return;

        for (BunkerInstance.HologramInfo info : holograms) {
            String templateName = info.type;
            Location targetLocation = info.location;
            targetLocation.setWorld(world); // assign world

            Hologram template = DHAPI.getHologram(templateName);
            if (template == null) {
                Bukkit.getLogger().warning("Template hologram '" + templateName + "' not found. Skipping.");
                continue;
            }

            String cloneName = world.getName() + "_" + templateName + "_" + System.currentTimeMillis();
            Hologram clone = template.clone(cloneName, targetLocation, false); // false = don't overwrite

            clone.enable();
            clone.save();

            Bukkit.getLogger().info("Cloned hologram '" + templateName + "' as '" + cloneName + "' at " + targetLocation);
        }
    }


}
