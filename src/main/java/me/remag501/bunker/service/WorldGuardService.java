package me.remag501.bunker.service;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.remag501.bunker.Bunker;
import org.bukkit.World;

public class WorldGuardService {

    private final Bunker plugin;

    public WorldGuardService(Bunker plugin) {
        this.plugin = plugin;
    }

    public void setupBunkerFlags(World world) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(world));

            if (regionManager == null) {
                plugin.getLogger().warning("Could not access RegionManager for world: " + world.getName());
                return;
            }

            ProtectedRegion globalRegion = regionManager.getRegion("__global__");
            if (globalRegion == null) {
                globalRegion = new GlobalProtectedRegion("__global__");
                regionManager.addRegion(globalRegion);
            }

            applyFlags(globalRegion);
            plugin.getLogger().info("Applied bunker WorldGuard flags to " + world.getName());

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to setup WorldGuard flags for " + world.getName() + ": " + e.getMessage());
        }
    }

    private void applyFlags(ProtectedRegion region) {
        // Core Flags
        setFlag(region, "ice-melt", StateFlag.State.DENY);
        setFlag(region, "enderpearl", StateFlag.State.DENY);
        setFlag(region, "block-break", StateFlag.State.DENY);
        setFlag(region, "block-place", StateFlag.State.DENY);
        setFlag(region, "pvp", StateFlag.State.DENY);
        setFlag(region, "fall-damage", StateFlag.State.DENY);
        setFlag(region, "lava-flow", StateFlag.State.DENY);
        setFlag(region, "lava-fire", StateFlag.State.DENY);
        setFlag(region, "invincible", StateFlag.State.ALLOW);

        // Custom Plugin Hooks (Mythic, etc.)
        setFlag(region, "mi-weapons", StateFlag.State.DENY);
        setFlag(region, "mmo-abilities", StateFlag.State.DENY);
    }

    private void setFlag(ProtectedRegion region, String flagName, StateFlag.State state) {
        StateFlag flag = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get(flagName);
        if (flag != null) {
            region.setFlag(flag, state);
        }
    }
}