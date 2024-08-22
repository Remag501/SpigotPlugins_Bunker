package me.remag501.bunker.commands;

import me.remag501.bunker.Bunker;
import me.remag501.bunker.util.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            return visit(); // Will need args[1] for the bunker name
        if (args.length > 1 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("add"))
            return addBunkers(Integer.parseInt(args[2]), sender); // Create more bunkers using multicore
        if (args.length > 0) {
            sender.sendMessage("Invalid arguments! Use /bunker [buy/home/visit]");
            return true;
        }
        // No arguments
        return bunkerHome(sender);
    }

    public boolean assignBunker(Player buyer) {
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
    public boolean visit() {
        return true; // Replace with actual implementation
    }

    public boolean bunkerHome(CommandSender sender) {
        // Handle the default case: no arguments or non-reload arguments
        String message = plugin.getConfig().getString("message", "Default message");
        Player player = (Player) sender;
        player.sendMessage(message);
        return true; // Replace with actual implementation

    }

    public boolean addBunkers(int bunkers, CommandSender sender) {
        ((Player) sender).sendMessage("Added " + bunkers + " bunkers.");
        ConfigUtil config = new ConfigUtil(plugin,"bunkers.yml");
        int totalBunkers = config.getConfig().getInt("totalBunkers");
        totalBunkers += bunkers;
        config.getConfig().set("totalBunkers", totalBunkers);
        config.save();
        // Implement world creation
        return true;
    }

}
