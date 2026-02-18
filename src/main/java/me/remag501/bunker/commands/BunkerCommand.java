package me.remag501.bunker.commands;

import me.remag501.bunker.Bunker;
import me.remag501.bunker.managers.BunkerCreationManager;
import me.remag501.bunker.managers.ConfigManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BunkerCommand implements CommandExecutor {
    private final Bunker plugin;
    private final ConfigManager configManager;
    private final BunkerCreationManager bunkerCreationManager;

    public BunkerCommand(Bunker plugin, ConfigManager configManager, BunkerCreationManager bunkerCreationManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.bunkerCreationManager = bunkerCreationManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        Player player = (Player) sender;
        String playerName = player.getName();

        if (!player.hasPermission("bunker.use"))
            return true;

        if (args.length == 0 || args[0].equalsIgnoreCase("home")) {
            // Teleport to own bunker
            if (!bunkerCreationManager.hasBunker(playerName)) {
                player.sendMessage(configManager.getMessage("noBunker"));
                return true;
            }
            String worldName = bunkerCreationManager.getWorldName(playerName);
            World bunkerWorld = plugin.getServer().getWorld(worldName);
            if (bunkerWorld == null) {
                player.sendMessage("Bunker world not found!");
                return true;
            }
            // Teleport logic, e.g., to spawn or configured coords
            Location loc = bunkerWorld.getSpawnLocation();
            player.teleport(loc);
            player.sendMessage(configManager.getMessage("homeMsg"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "buy":
                if (bunkerCreationManager.hasBunker(playerName)) {
                    player.sendMessage(configManager.getMessage("alreadyOwnBunker"));
                    return true;
                }
                if (bunkerCreationManager.assignBunker(playerName)) {
                    player.sendMessage(configManager.getMessage("bunkerPurchased"));
                } else {
                    player.sendMessage(configManager.getMessage("outOfBunkers"));
                }
                return true;

//            case "visit":
//                player.sendMessage("This command is temporarily removed");
//                return true;

//            case "accept":
//                if (!visitRequestManager.hasPendingRequest(player.getUniqueId())) {
//                    player.sendMessage("You have no pending visit requests.");
//                    return true;
//                }
//                Player requester = visitRequestManager.getPendingRequest(player.getUniqueId());
//                if (requester != null) {
//                    // Teleport visitor to bunker owner
//                    String ownerWorldName = bunkerCreationManager.getWorldName(playerName);
//                    World ownerWorld = plugin.getServer().getWorld(ownerWorldName);
//                    if (ownerWorld != null) {
//                        Location spawn = ownerWorld.getSpawnLocation();
//                        requester.teleport(spawn);
//                        requester.sendMessage("You have been teleported to " + playerName + "'s bunker.");
//                        player.sendMessage("You accepted the visit request.");
//                    } else {
//                        player.sendMessage("Bunker world not found.");
//                    }
//                } else {
//                    player.sendMessage("Requester is not online.");
//                }
//                return true;
//
//            case "decline":
//                if (!visitRequestManager.hasPendingRequest(player.getUniqueId())) {
//                    player.sendMessage("You have no pending visit requests.");
//                    return true;
//                }
//                visitRequestManager.removeRequest(player.getUniqueId());
//                player.sendMessage("You declined the visit request.");
//                return true;

            default:
                player.sendMessage(configManager.getMessage("argCommandUsage"));
                return true;
        }
    }

}
