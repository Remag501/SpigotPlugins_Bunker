package me.remag501.bunker.managers;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.remag501.bunker.core.BunkerInstance;
import org.bukkit.Location;
import org.bukkit.World;

public class HologramManager {

    public static void addHologram(BunkerInstance bunkerInstance, World world) {
        // Get the hologram name to clone
//        String templateName = bunkerInstance.getHologramName();
//        Location targetLocation = bunkerInstance.getHologramLocation();
//
//        // Ensure the location is assigned to the correct world
//        if (targetLocation.getWorld() == null) {
//            targetLocation.setWorld(world);
//        }
//
//        // Retrieve the original hologram by name
//        Hologram template = DHAPI.getHologram(templateName);
//        if (template == null) {
//            System.out.println("Template hologram '" + templateName + "' not found. Skipping hologram clone.");
//            return;
//        }
//
//        // Optional: generate a unique name for the new hologram
//        String newHologramName = templateName + "_clone_" + System.currentTimeMillis();
//
//        // Clone the hologram to the new location
////        Hologram cloned = DHAPI.createHologram(newHologramName, targetLocation, template.get);
//        Hologram cloned = template.clone(world.getName() + template.getName(), targetLocation, false);
//        cloned.enable();
//        cloned.save();
//
//        System.out.println("Cloned hologram '" + templateName + "' to '" + newHologramName + "' at " + targetLocation);
    }

}
