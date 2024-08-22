package me.remag501.bunker.commands;

import me.remag501.bunker.util.Schematic;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import me.remag501.bunker.Bunker;
import me.remag501.bunker.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.WorldType;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.bukkit.Bukkit.getLogger;

public class BunkerCommand implements CommandExecutor {

    private final Bunker plugin;

    public BunkerCommand(Bunker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // May remove
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by players.");
            return true;
        }
        // Handle the "reload" argument
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            // Reload the configuration
            plugin.reloadConfig();
            sender.sendMessage("Configuration reloaded successfully.");
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("buy"))
            return assignBunker((Player) sender);
        if (args.length > 0 && args[0].equalsIgnoreCase("home"))
            return bunkerHome(sender);
        // Allows players to visit the bunker by using the "visit" argument
        if (args.length > 0 && args[0].equalsIgnoreCase("visit"))
            return visit(sender); // Will need args[1] for the bunker name
        if (args.length > 1 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("add"))
            return addBunkers(Integer.parseInt(args[2]), sender); // Create more bunkers using multicore
        if (args.length > 0) {
            sender.sendMessage("Invalid arguments! Use /bunker [buy/home/visit]");
            return true;
        }
        // No arguments
        return bunkerHome(sender);
    }

    private boolean assignBunker(Player buyer) {
        ConfigUtil config = new ConfigUtil(plugin, "bunkers.yml");
        int assignedBunkers = config.getConfig().getInt("assignedBunkers");
        int totalBunkers = config.getConfig().getInt("totalBunkers");
        if (assignedBunkers == totalBunkers) {
            buyer.sendMessage("Fatal error: Out of storage for bunkers. Contact an admin to help you out!");
            return true;
        } else if (config.getConfig().contains(buyer.getName())) {
            buyer.sendMessage("You have already purchased a bunker!");
            return true;
        }
        else {
            // Decrease the available bunkers count
            config.getConfig().set("assignedBunkers", assignedBunkers + 1);
            config.getConfig().set(buyer.getName(), assignedBunkers);
            config.save();
            buyer.sendMessage("You have successfully purchased a bunker!");
            return true;
        }
    }
    private boolean visit(CommandSender sender) {
        createBunkerWorld(sender);
        return true; // Replace with actual implementation
    }

    private boolean bunkerHome(CommandSender sender) {
        // Handle the default case: no arguments or non-reload arguments
        String message = plugin.getConfig().getString("message", "Default message");
        Player player = (Player) sender;
        player.sendMessage(message);
        return true; // Replace with actual implementation

    }

    private boolean addBunkers(int bunkers, CommandSender sender) {
        ((Player) sender).sendMessage("Added " + bunkers + " bunkers.");
        ConfigUtil config = new ConfigUtil(plugin,"bunkers.yml");
        int totalBunkers = config.getConfig().getInt("totalBunkers");
        totalBunkers += bunkers;
        config.getConfig().set("totalBunkers", totalBunkers);
        config.save();
        // Implement world creation
        return true;
    }

    private void createBunkerWorld(CommandSender sender) {
        File schematicFile = new File(plugin.getDataFolder(), "schematics/bunker.schem");
        if (!schematicFile.exists()) {
            ((Player) sender).sendMessage("No schematic found. Make sure you named it correctly and its placed in schematics/bunker.schem");
            return;
        }
        Plugin multiversePlugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        MultiverseCore multiverseCore = (MultiverseCore) multiversePlugin;
        MVWorldManager worldManager = multiverseCore.getMVWorldManager();
//        // Define the world name and type
        String worldName = "new_world";
//        WorldType
//        // Create the world
        if (worldManager.getMVWorld(worldName) == null) {
            worldManager.addWorld(
                    worldName, // name of world
                    World.Environment.NORMAL, // enviornment
                    null, // seed
                    WorldType.FLAT, // World Type
                    false, // Generate structures
                    "VoidGen"); // Custom generator
            plugin.getLogger().info("World " + worldName + " created successfully.");
        } else {
            plugin.getLogger().info("World " + worldName + " already exists.");
            return;
        }
        // Add schematic to empty world
        Location pasteLocation = new Location(Bukkit.getWorld(worldName), 0, 0, 0);
        Schematic schematic = new Schematic(pasteLocation, schematicFile);
        schematic.loadAndPasteSchematic();
    }

}
