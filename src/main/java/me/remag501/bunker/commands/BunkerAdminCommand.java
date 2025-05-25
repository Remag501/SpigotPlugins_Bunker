package me.remag501.bunker.commands;

import me.remag501.bunker.Bunker;
import me.remag501.bunker.util.AdminManager;
import me.remag501.bunker.util.BunkerCreationManager;
import me.remag501.bunker.util.ConfigManager;
import me.remag501.bunker.util.VisitRequestManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BunkerAdminCommand implements CommandExecutor {

    private final Bunker plugin;
    private final ConfigManager configManager;
    private final VisitRequestManager visitRequestManager;
    private final BunkerCreationManager bunkerCreationManager;
    private final AdminManager adminManager;

    public BunkerAdminCommand(Bunker plugin) {
        this.plugin = plugin;
        this.configManager = new ConfigManager(plugin);
        this.visitRequestManager = new VisitRequestManager(plugin);
        this.bunkerCreationManager = new BunkerCreationManager(plugin, configManager);
        this.adminManager = new AdminManager(plugin, bunkerCreationManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        Player player = (Player) sender;
        String playerName = player.getName();

        if (!player.hasPermission("bunker.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /bunkeradmin <add|preview|migrate|upgrade>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /bunkeradmin add <amount>");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[1]);
                    if (amount <= 0) {
                        player.sendMessage(ChatColor.RED + "Amount must be a positive number.");
                        return true;
                    }
                    bunkerCreationManager.addBunkers(amount, sender);
                    player.sendMessage(ChatColor.GREEN + "Starting creation for " + amount + " bunkers.");
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid number.");
                }
                return true;

            case "preview":
                // Load or teleport the admin to a preview world, you can modify this logic
                adminManager.previewBunker(player);
//                World previewWorld = Bukkit.getWorld("bunker_preview");
//                if (previewWorld == null) {
//                    player.sendMessage(ChatColor.RED + "Preview world not found.");
//                    return true;
//                }
//                player.teleport(previewWorld.getSpawnLocation());
//                player.sendMessage(ChatColor.GREEN + "Teleported to preview world.");
                return true;

            case "migrate":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /bunkeradmin migrate <amount>");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[1]);
                    if (amount <= 0) {
                        player.sendMessage(ChatColor.RED + "Amount must be positive.");
                        return true;
                    }
                    player.sendMessage(ChatColor.GRAY + "Migration has not been added yet");
//                    // Implement your migration logic here
//                    boolean success = bunkerCreationManager.migrateOldFormat(amount);
//                    if (success) {
//                        player.sendMessage(ChatColor.GREEN + "Migrated " + amount + " entries.");
//                    } else {
//                        player.sendMessage(ChatColor.RED + "Migration failed or incomplete.");
//                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid number.");
                }
                return true;

            case "upgrade":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /bunkeradmin upgrade <level>");
                    return true;
                }

//                if (!bunkerCreationManager.hasBunker(playerName)) {
//                    player.sendMessage(ChatColor.RED + "You do not own a bunker.");
//                    return true;
//                }
//
//                String level = args[1];
//                String worldName = bunkerCreationManager.getWorldName(playerName);
//                String currentWorld = player.getWorld().getName();
//
//                if (!currentWorld.equals(worldName) && !currentWorld.equals("bunker_preview")) {
//                    player.sendMessage(ChatColor.RED + "You must be in your bunker or preview to upgrade.");
//                    return true;
//                }
//
//                if (bunkerCreationManager.upgradeBunker(playerName, level)) {
//                    player.sendMessage(ChatColor.GREEN + "Bunker upgraded with: " + level);
//                } else {
//                    player.sendMessage(ChatColor.RED + "Upgrade failed. Already applied or invalid.");
//                }
                player.sendMessage(ChatColor.GRAY + "Upgrade not added yet");
                return true;

            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use add, preview, migrate, or upgrade.");
                return true;
        }
    }

}
