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

public class GeneratorManager {

    public static void createGenerator(Player player, World world, BunkerInstance bunkerInstance) {

//        if (bunkerInstance.getGeneratorType() == null)
//            return;
//
//        Location l = bunkerInstance.getGeneratorLocation();
//        l.setWorld(world);
//
//        GeneratorLocation gl = new GeneratorLocation(-1, Main.getGenerators().get(bunkerInstance.getGeneratorType()), l,
//                Main.getPlacedGenerators().new ChunkInfo(l.getChunk()),
//                Main.getPlayers().getPlayer(player.getName()), null);
//
//        gl.placeGenerator(player, true);

    }

}
