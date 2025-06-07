package me.remag501.bunker.managers;

import me.kryniowesegryderiusz.kgenerators.Main;
import me.kryniowesegryderiusz.kgenerators.api.KGeneratorsAPI;
import me.kryniowesegryderiusz.kgenerators.api.interfaces.IGeneratorLocation;
import me.kryniowesegryderiusz.kgenerators.api.objects.AbstractGeneratedObject;
import me.kryniowesegryderiusz.kgenerators.generators.generator.GeneratorsManager;
import me.kryniowesegryderiusz.kgenerators.generators.generator.enums.GeneratorType;
import me.kryniowesegryderiusz.kgenerators.generators.generator.objects.GeneratedBlock;
import me.kryniowesegryderiusz.kgenerators.generators.generator.objects.Generator;
import me.kryniowesegryderiusz.kgenerators.generators.locations.objects.GeneratorLocation;
import me.remag501.bunker.Bunker;
import me.remag501.bunker.core.BunkerInstance;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class GeneratorManager {

    public static void createGenerator(Player player, World world, BunkerInstance bunkerInstance) {
        List<BunkerInstance.GeneratorInfo> generators = bunkerInstance.getGenerators();
        if (generators == null || generators.isEmpty()) return;

        for (BunkerInstance.GeneratorInfo info : generators) {
            String type = info.type;
            Location loc = info.location;
            loc.setWorld(world);

            Generator generator = Main.getGenerators().get(type);
            if (generator == null) {
                Bukkit.getLogger().warning("Generator type '" + type + "' not found.");
                continue;
            }

            GeneratorLocation genLoc = new GeneratorLocation(
                    -1,
                    generator,
                    loc,
                    Main.getPlacedGenerators().new ChunkInfo(loc.getChunk()),
                    Main.getPlayers().getPlayer(player.getName()),
                    null
            );

            genLoc.placeGenerator(player, true);
            Bukkit.getLogger().info("Placed generator '" + type + "' at " + loc);
        }
    }


}
