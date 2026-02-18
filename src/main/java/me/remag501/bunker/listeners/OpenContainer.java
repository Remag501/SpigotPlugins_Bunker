package me.remag501.bunker.listeners;

import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.guis.VaultSelector;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.EnumSet;
import java.util.Set;

public class OpenContainer implements Listener {

    @EventHandler
    public void onStorageAccess(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        World world = clickedBlock.getWorld();
        if (!world.getName().startsWith("bunker_")) return;

        Material type = clickedBlock.getType();

        // Your hand-picked list of blocks that should open vaults
        Set<Material> blockedContainers = EnumSet.of(
                Material.CHEST,
                Material.TRAPPED_CHEST,
                Material.BARREL,
                Material.SHULKER_BOX,
                Material.WHITE_SHULKER_BOX,
                Material.ORANGE_SHULKER_BOX,
                Material.MAGENTA_SHULKER_BOX,
                Material.LIGHT_BLUE_SHULKER_BOX,
                Material.YELLOW_SHULKER_BOX,
                Material.LIME_SHULKER_BOX,
                Material.PINK_SHULKER_BOX,
                Material.GRAY_SHULKER_BOX,
                Material.LIGHT_GRAY_SHULKER_BOX,
                Material.CYAN_SHULKER_BOX,
                Material.PURPLE_SHULKER_BOX,
                Material.BLUE_SHULKER_BOX,
                Material.BROWN_SHULKER_BOX,
                Material.GREEN_SHULKER_BOX,
                Material.RED_SHULKER_BOX,
                Material.BLACK_SHULKER_BOX,
                Material.HOPPER,
                Material.DROPPER,
                Material.ENDER_CHEST
        );

        // Only proceed if it's a valid container (has an inventory)
        BlockState state = clickedBlock.getState();
        if (blockedContainers.contains(type)) {
            event.setCancelled(true);
            VaultManager.getPlayer(event.getPlayer()).thenAccept(vaultPlayer -> {
                new VaultSelector(event.getPlayer(), vaultPlayer).open();
            });
            return;
        }

        // GUI-opening but non-container blocks (crafting tables, etc.)
        Set<Material> blockedMenus = Set.of(
                Material.SMITHING_TABLE,
                Material.BREWING_STAND,
                Material.ENCHANTING_TABLE,
                Material.ANVIL,
                Material.CHIPPED_ANVIL,
                Material.DAMAGED_ANVIL,
                Material.GRINDSTONE
        );

        if (blockedMenus.contains(type) || state instanceof Container) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c§l(!) §cYou can't use that in a bunker world.");
        }
    }

}
