package me.remag501.bunker.commands;

import me.remag501.bunker.Bunker;
import me.remag501.bunker.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BunkerCommand implements CommandExecutor {
    private final Bunker plugin;
    private final ConfigManager configManager;
    private final VisitRequestManager visitRequestManager;
    private final BunkerCreationManager bunkerCreationManager;

    public BunkerCommand(Bunker plugin) {
        this.plugin = plugin;
        this.configManager = new ConfigManager(plugin);
        this.visitRequestManager = new VisitRequestManager(plugin);
        this.bunkerCreationManager = new BunkerCreationManager(plugin, configManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        Player player = (Player) sender;
        String playerName = player.getName();

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

            case "upgrade":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /bunker upgrade <level>");
                    return true;
                }
                if (!bunkerCreationManager.hasBunker(playerName)) {
                    player.sendMessage(configManager.getMessage("noBunkerOwned"));
                    return true;
                }

                String level = args[1];
                String bunkerName = bunkerCreationManager.getWorldName(playerName);
                String playerWorldName = player.getWorld().getName();
                if (!playerWorldName.equals(bunkerName) && !playerWorldName.equals("bunker_preview")) {
                    player.sendMessage(ChatColor.RED + "You need to be in your bunker to upgrade it!");
                    return true;
                }

                if (bunkerCreationManager.upgradeBunker(playerName, level))
                    player.sendMessage(ChatColor.GREEN + "Your bunker has gotten the upgrade " + level + "!");
                else
                    player.sendMessage(ChatColor.RED + "You already have the upgrade " + level + " or it does not exist!");
                return true;

            case "visit":
                if (args.length < 2) {
                    player.sendMessage(configManager.getMessage("visitCommandUsage"));
                    return true;
                }
                String targetName = args[1];
                if (!bunkerCreationManager.hasBunker(targetName)) {
                    player.sendMessage(configManager.getMessage("noBunker"));
                    return true;
                }

                Player targetPlayer= Bukkit.getPlayer(targetName);
                if (targetPlayer == null) {
                    player.sendMessage("That player does not exist or is not online");
                    return true;
                }

                UUID targetUUID = Bukkit.getPlayer(targetName).getUniqueId(); // or cached UUID method
                if (visitRequestManager.hasPendingRequest(targetUUID)) {
                    player.sendMessage("That player already has a pending visit request.");
                    return true;
                }

                visitRequestManager.addRequest(player.getUniqueId(), targetPlayer, bunkerCreationManager.getWorldName(playerName));
                player.sendMessage("Visit request sent to " + targetName + ".");
                if (targetPlayer != null) {
                    targetPlayer.sendMessage(player.getName() + " wants to visit your bunker! Use /bunker accept or /bunker decline.");
                }
                return true;

            case "accept":
                if (!visitRequestManager.hasPendingRequest(player.getUniqueId())) {
                    player.sendMessage("You have no pending visit requests.");
                    return true;
                }
                Player requester = visitRequestManager.getPendingRequest(player.getUniqueId());
                if (requester != null) {
                    // Teleport visitor to bunker owner
                    String ownerWorldName = bunkerCreationManager.getWorldName(playerName);
                    World ownerWorld = plugin.getServer().getWorld(ownerWorldName);
                    if (ownerWorld != null) {
                        Location spawn = ownerWorld.getSpawnLocation();
                        requester.teleport(spawn);
                        requester.sendMessage("You have been teleported to " + playerName + "'s bunker.");
                        player.sendMessage("You accepted the visit request.");
                    } else {
                        player.sendMessage("Bunker world not found.");
                    }
                } else {
                    player.sendMessage("Requester is not online.");
                }
                return true;

            case "decline":
                if (!visitRequestManager.hasPendingRequest(player.getUniqueId())) {
                    player.sendMessage("You have no pending visit requests.");
                    return true;
                }
                visitRequestManager.removeRequest(player.getUniqueId());
                player.sendMessage("You declined the visit request.");
                return true;

            case "reload":
                configManager.reload();
                bunkerCreationManager.reloadBunkerConfig();
                player.sendMessage("Bunker config reloaded.");
                return true;

            case "admin":
                if (args.length < 3 || !args[1].equalsIgnoreCase("add")) {
                    player.sendMessage("Usage: /bunker admin add <number>");
                    return true;
                }
                int number;
                try {
                    number = Integer.parseInt(args[2]);
                    if (number <= 0) {
                        player.sendMessage("Number must be positive.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid number.");
                    return true;
                }

                // Delegate bunker creation to BunkerCreationManager
                bunkerCreationManager.addBunkers(number, sender);
                return true;



            default:
                player.sendMessage(configManager.getMessage("argCommandUsage"));
                return true;
        }
    }

}
