package me.remag501.bunker.service;

import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.generators.ActiveGenerator;
import com.muhammaddaffa.nextgens.generators.Generator;
import me.remag501.bunker.core.BunkerInstance;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class GeneratorService {

    public void createGenerator(Player player, World world, BunkerInstance bunkerInstance) {
        // 1. Get the NextGens API/Instance
        NextGens nextGensPlugin = NextGens.getInstance();
        if (nextGensPlugin == null) {
            Bukkit.getLogger().warning("[Bunker] NextGens instance is null!");
            return;
        }

        // 2. Get the GeneratorManager from NextGens
        com.muhammaddaffa.nextgens.generators.managers.GeneratorManager manager = nextGensPlugin.getGeneratorManager();

        List<BunkerInstance.GeneratorInfo> generators = bunkerInstance.getGenerators();
        if (generators == null || generators.isEmpty()) {
            return;
        }

        for (BunkerInstance.GeneratorInfo info : generators) {
            String type = info.type;
            Location loc = info.location.clone(); // Clone to prevent modifying the original reference
            loc.setWorld(world);

            // 3. Find the generator template
            Generator generator = manager.getGenerator(type);
            if (generator == null) {
                Bukkit.getLogger().warning("[Bunker] Generator type '" + type + "' not found in NextGens!");
                continue;
            }

            // 4. Use Native Paper API for Async Chunk Loading (Replaces PaperLib)
            // This fixes the NoClassDefFoundError
            world.getChunkAtAsync(loc).thenAccept(chunk -> {
                Block block = loc.getBlock();

                try {
                    // 5. Register the generator
                    // Using the player as the owner so it counts towards their limits/stats
                    ActiveGenerator activeGenerator = manager.registerGenerator(player, generator, block);
                    double mainTimer = activeGenerator.getTimer();
                    activeGenerator.setTimer(20); // Make it spawn instantly
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            activeGenerator.setTimer(mainTimer);
                        }
                    }.runTaskLater(Bukkit.getPluginManager().getPlugin("Bunker"), 20);

                    activeGenerator.setTimer(20);
                    activeGenerator.setCorrupted(false);


                    Bukkit.getLogger().info("[Bunker] Registered " + type + " at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                } catch (Exception e) {
                    Bukkit.getLogger().severe("[Bunker] Failed to register generator: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }


}

