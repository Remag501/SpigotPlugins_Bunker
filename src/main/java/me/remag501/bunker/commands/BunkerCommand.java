package me.remag501.bunker.commands;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import me.remag501.bunker.util.Schematic;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import me.remag501.bunker.Bunker;
import me.remag501.bunker.util.ConfigUtil;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.WorldType;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.bukkit.Bukkit.getLogger;

public class BunkerCommand implements CommandExecutor {

    private final Bunker plugin;
    private Set<UUID> runningTasks = new HashSet<>();
    private HashMap<String, String> messages = new HashMap<String, String>();
//    private final HashMap<String, Integer> configInts = new HashMap<String, Integer>();
    private HashMap<String, Double> doubles = new HashMap<String, Double>();

    private Schematic schematic;
    private Clipboard clipboard;
    private File schematicFile;

    public BunkerCommand(Bunker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by players.");
            return true;
        }
        // No arguments
        if (args.length == 0) {
            return bunkerHome(sender);
        }
        switch (args[0].toLowerCase()) {
            case "buy": // Allows players to buy a bunker by using the "buy" argument
                return assignBunker((Player) sender);
            case "home": // Allows players to go to their bunker home by using the "home" argument
                return bunkerHome(sender);
            case "visit": // Allows players to visit the bunker by using the "visit" argument
                if (args.length == 2) {
                    return visit(sender, args[1]); // Will need args[1] for the player name
                } else {
                    sender.sendMessage("Usage: /bunker visit [player]");
                    return true;
                }
            case "reload": // Handle the "reload" argument
                return reload(sender);
            case "admin": // Create more bunkers using multicore
                if (args.length == 3 && args[1].equalsIgnoreCase("add")) {
                    return addBunkers(Integer.parseInt(args[2]), sender);
                }
                break;
            default:
                sender.sendMessage("Invalid arguments! Use /bunker [buy/home/visit]");
                return true;
        }
        return false;
    }

    private boolean test(CommandSender sender) {
        Player player = (Player) sender;
        // Copy the NPC
        NPC npc = CitizensAPI.getNPCRegistry().getById(0);
        NPC clone = npc.clone();
        // Set the location of the cloned NPC
        Location newLocation = player.getLocation(); // Provide the x, y, z coordinates
        clone.teleport(newLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
        // Spawn the cloned NPC at the new location
        clone.spawn(newLocation);
        return true;
    }

    public boolean reload(CommandSender sender) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        ConfigUtil bunker = new ConfigUtil(plugin, "bunkers.yml");
        bunker.save();
        // Load messages from config
        messages.put("noBunkers", config.getString("noBunkers"));
        messages.put("alreadyPurchased", config.getString("alreadyPurchased"));
        messages.put("bunkerPurchased", config.getString("bunkerPurchased"));
        messages.put("playerNotExist", config.getString("playerNotExist"));
        messages.put("visitMsg", config.getString("visitMsg"));
        messages.put("noBunker", config.getString("noBunker"));
        messages.put("homeMsg", config.getString("homeMsg"));
        // Format strings in messages, need command executors for more advanced formatting
//        for (String message: messages.values()) {
//            messages.put(message, messages.get(message).replace("%player%", sender.getName()));
//        }
        // Load doubles from config
        doubles.put("x", config.getDouble("x"));
        doubles.put("y", config.getDouble("y"));
        doubles.put("z", config.getDouble("z"));
        doubles.put("spawnX", config.getDouble("spawnX"));
        doubles.put("spawnY", config.getDouble("spawnY"));
        doubles.put("spawnZ", config.getDouble("spawnZ"));
        doubles.put("yaw", config.getDouble("yaw"));
        doubles.put("pitch", config.getDouble("pitch"));
        doubles.put("npcId", config.getDouble("npcId"));
        doubles.put("npcX", config.getDouble("npcCoords.x"));
        doubles.put("npcY", config.getDouble("npcCoords.y"));
        doubles.put("npcZ", config.getDouble("npcCoords.z"));
        doubles.put("npcYaw", config.getDouble("npcCoords.yaw"));
        doubles.put("npcPitch", config.getDouble("npcCoords.pitch"));
        // Check if schematic file exists
        schematicFile = new File(plugin.getDataFolder(), "schematics/bunker.schem");
        if (!schematicFile.exists()) {
            if (sender == null)
                plugin.getLogger().info("No schematic found. Make sure you named it correctly and its placed in schematics/bunker.schem");
            else
                sender.sendMessage("No schematic found. Make sure you named it correctly and its placed in schematics/bunker.schem");
            return true;
        }
        // Reload schematic
        schematic = new Schematic(schematicFile, plugin);
        clipboard = schematic.loadSchematic(schematicFile);

        if (sender == null)
            plugin.getLogger().info("Configuration reloaded successfully.");
        else
            sender.sendMessage("Configuration reloaded successfully.");
        return true;
    }

    private boolean addNPC(CommandSender sender, World world) {
        // Get config message strings
        int npcID = doubles.get("npcId").intValue();
        double npcX = doubles.get("npcX");
        double npcY = doubles.get("npcZ");
        double npcZ = doubles.get("npcY");
        float npcYaw = doubles.get("npcYaw").floatValue();
        float npcPitch = doubles.get("npcPitch").floatValue();
        // Copy the NPC
        NPC npc = CitizensAPI.getNPCRegistry().getById(npcID);
        NPC clone = npc.clone();
        // Set the location of the cloned NPC
        Location newLocation = new Location(world, npcX, npcY, npcZ); // Provide the x, y, z coordinates
        newLocation.setYaw(npcYaw);
        newLocation.setPitch(npcPitch);
        // Override location of cloned npc to new location
        clone.teleport(newLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
        // Spawn the cloned NPC at the new location
        clone.spawn(newLocation);
        plugin.getLogger().info("Added npc to world!");
        return true;
    }

    private boolean assignBunker(Player buyer) {
        // Get config message strings
        String noBunkers = messages.get("noBunkers"),
                alreadyPurchased = messages.get("alreadyPurchased"),
                bunkerPurchased = messages.get("bunkerPurchased");
        // Check if the player has enough storage for bunkers and has not already bought one
        ConfigUtil config = new ConfigUtil(plugin, "bunkers.yml");
        int assignedBunkers = config.getConfig().getInt("assignedBunkers");
        int totalBunkers = config.getConfig().getInt("totalBunkers");
        if (assignedBunkers == totalBunkers) {
            buyer.sendMessage(noBunkers);
            return true;
        } else if (config.getConfig().contains(buyer.getName())) {
            buyer.sendMessage(alreadyPurchased);
            return true;
        }
        else {
            // Decrease the available bunkers count
            config.getConfig().set("assignedBunkers", assignedBunkers + 1);
            config.getConfig().set(buyer.getName(), assignedBunkers);
            config.save();
            buyer.sendMessage(bunkerPurchased);
            return true;
        }
    }
    private boolean visit(CommandSender sender, String playerName) {
        // Get config message strings
        String playerNotExist = messages.get("playerNotExist"),
                visitMsg = messages.get("visitMsg"); // Revisit
        // Format message strings
        playerNotExist = playerNotExist.replace("%player%", playerName);
        visitMsg = visitMsg.replace("%player%", playerName);
        // Check if the player exists
        ConfigUtil config = new ConfigUtil(plugin, "bunkers.yml");
        if (!config.getConfig().contains(playerName)) {
            sender.sendMessage(playerNotExist);
            return true;
        }
        // Teleport player to their bunker
        String worldName = "bunker_" + config.getConfig().getString(playerName);
        sender.sendMessage(visitMsg);
        teleportPlayer((Player) sender, worldName);
        return true;
    }

    private boolean bunkerHome(@NotNull CommandSender sender) {
        // Get config message strings
        String noBunker = messages.get("noBunkers");
        String homeMsg = messages.get("homeMsg");
        // Handle the default case: no arguments or non-reload arguments
        ConfigUtil config = new ConfigUtil(plugin, "bunkers.yml");
        // Check if player has a bunker
        if (!config.getConfig().contains(sender.getName())) {
            sender.sendMessage(noBunker);
            return true;
        }
        // Send player teleport message
        Player player = (Player) sender;
        player.sendMessage(homeMsg);
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
        // Get UUID and playerID
        Player player = null;
        UUID playerId = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            playerId = player.getUniqueId();
        }
        // Check if the player already triggered the command
        if (runningTasks.contains(playerId)) {
            player.sendMessage("The bunker creation is still in progress. Please wait.");
            return true;
        }
        runningTasks.add(playerId);
        // Update bunker count in config
        ConfigUtil config = new ConfigUtil(plugin,"bunkers.yml");
        int totalBunkers = config.getConfig().getInt("totalBunkers");
        config.getConfig().set("totalBunkers", totalBunkers + bunkers);
        config.save();
        // Create the bunkers on a different world
        for (int i = 0; i < bunkers; i++)
            createBunkerWorld(sender, "bunker_" + (totalBunkers + i));
        sender.sendMessage("Created " + bunkers + " bunkers.");
        return true;
    }

    private void createBunkerWorld(CommandSender sender, String worldName) {
        // Get config message strings
        int x = doubles.get("x").intValue();
        int y = doubles.get("y").intValue();
        int z = doubles.get("z").intValue();
        double spawnX = doubles.get("spawnX");
        double spawnY = doubles.get("spawnY");
        double spawnZ = doubles.get("spawnZ");
        float yaw = doubles.get("yaw").floatValue();
        float pitch = doubles.get("pitch").floatValue();
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
        // Add schematic to empty world via multithreading
        UUID playerId = ((Player) sender).getUniqueId();
        Location pasteLocation = new Location(Bukkit.getWorld(worldName), x, y, z);
        schematic.setLocation(pasteLocation);
        schematic.pasteSchematic(clipboard, pasteLocation);
        // Not running anything asynchronously currently *may remove*
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Load schematic file
            } finally {
                // Remove the player from the set after the task is completed
//                sender.sendMessage("Loaded bunker schematics!");
                runningTasks.remove(playerId);
            }
                });
        // Set spawn location for new world
        Location newSpawn  = new Location(Bukkit.getWorld(worldName), spawnX, spawnY, spawnZ, yaw, pitch);
        World world = Bukkit.getWorld(worldName);
        // Disable Multiverse-Core's safe spawn enforcement
        MultiverseWorld mvWorld = worldManager.getMVWorld(world);
        mvWorld.setAdjustSpawn(false); // Disable safe teleport for this world
        mvWorld.setSpawnLocation(newSpawn); // multiverse world spawn
        world.setSpawnLocation(newSpawn); // Bukkit world spawn, wont set server spawn unless in main world
        mvWorld.setDifficulty(Difficulty.PEACEFUL); // Set difficulty to peaceful
        mvWorld.setGameMode(GameMode.ADVENTURE); // Set gamemode to adventure
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false); // Turns off mob spawning
        if (!(world.getSpawnLocation().getX() == spawnX && world.getSpawnLocation().getY() == spawnY && world.getSpawnLocation().getZ() == spawnZ))
            sender.sendMessage("Failed to set spawn location for world " + worldName + ". Check your configurtion.yml to adjust coordinates and make sure there are no obstructions, or it is not on air.");
        plugin.getLogger().info("World spawn set to " + newSpawn.toString());
        // Add npc to the bunker
        addNPC(sender, world);
    }

}
