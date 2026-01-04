package me.remag501.bunker.listeners;

import com.muhammaddaffa.nextgens.api.events.generators.GeneratorBreakEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GeneratorBreakListener implements Listener {

    @EventHandler
    public void handleGeneratorBreak(GeneratorBreakEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("bunker.breakgen")) {
            player.sendMessage("You can't do that sarr");
            event.setCancelled(true);
        }
    }


}
