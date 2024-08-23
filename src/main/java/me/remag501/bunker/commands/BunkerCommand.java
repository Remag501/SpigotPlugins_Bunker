package me.remag501.bunker.commands;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
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
        if (args.length == 1 && args[0].equalsIgnoreCase("visit")) {
            sender.sendMessage("Usage: /bunker visit [player]");
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("visit"))
            return visit(sender, args[1]); // Will need args[1] for the player name
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
    private boolean visit(CommandSender sender, String playerName) {
        ConfigUtil config = new ConfigUtil(plugin, "bunkers.yml");
        if (!config.getConfig().contains(playerName)) {
            sender.sendMessage("Player does not exist or have a bunker!");
            return true;
        }
        String worldName = "bunker_" + config.getConfig().getString(playerName);
        sender.sendMessage("Visiting " + playerName + "'s bunker");
        teleportPlayer((Player) sender, worldName);
        return true;
    }

    private boolean bunkerHome(CommandSender sender) {
        // Handle the default case: no arguments or non-reload arguments
        ConfigUtil config = new ConfigUtil(plugin, "bunkers.yml");
        // Check if player has a bunker
        if (!config.getConfig().contains(sender.getName())) {
            sender.sendMessage("You do not have a bunker. Use /bunker buy to purchase one.");
            return true;
        }
        // Send player teleport message
        String message = plugin.getConfig().getString("message", "Default message");
        Player player = (Player) sender;
        player.sendMessage(message);
        // Teleport player to their bunker
        // Get worldname
        String worldName = config.getConfig().getString(player.getName());
        worldName = "bunker_" + worldName;
        teleportPlayer(player, worldName);
        return true;
    }

    private void teleportPlayer(Player player, String worldName) {
        // Get the MVWorldManager
        MultiverseCore multiverseCore = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        MVWorldManager worldManager = multiverseCore.getMVWorldManager();
        // Get the target world
        MultiverseWorld targetWorld = worldManager.getMVWorld(worldName);
        // Teleport the player to the spawn location of the target world
        Location spawnLocation = targetWorld.getSpawnLocation();
        if (spawnLocation != null) {
            player.teleport(spawnLocation);
        } else {
            player.sendMessage("Spawn location not found in world: " + worldName + ". Contact an admin for help!");
        }
    }

    private boolean addBunkers(int bunkers, CommandSender sender) {
        // Check if schematic file exists
        File schematicFile = new File(plugin.getDataFolder(), "schematics/bunker.schem");
        if (!schematicFile.exists()) {
            ((Player) sender).sendMessage("No schematic found. Make sure you named it correctly and its placed in schematics/bunker.schem");
            return true;
        }
        // Update bunker count in config
        ConfigUtil config = new ConfigUtil(plugin,"bunkers.yml");
        int totalBunkers = config.getConfig().getInt("totalBunkers");
        config.getConfig().set("totalBunkers", totalBunkers + bunkers);
        config.save();
        // Create bunker worlds with schematic file
        for (int i = 0; i < bunkers; i++)
            createBunkerWorld(sender, "bunker_" + (totalBunkers + i), schematicFile);
        ((Player) sender).sendMessage("Added " + bunkers + " bunkers.");
        return true;
    }

    private void createBunkerWorld(CommandSender sender, String worldName, File schematicFile) {
        // Check if Multiverse-Core is installed
        Plugin multiversePlugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        MultiverseCore multiverseCore = (MultiverseCore) multiversePlugin;
        MVWorldManager worldManager = multiverseCore.getMVWorldManager();
        // Create the void world
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
        Schematic schematic = new Schematic(schematicFile, pasteLocation);
        schematic.loadAndPasteSchematic();
    }

}
