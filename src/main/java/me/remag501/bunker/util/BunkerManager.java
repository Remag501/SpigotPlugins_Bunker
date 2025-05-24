//package me.remag501.bunker.util;
//
//import me.remag501.bunker.Bunker;
//
//public class BunkerManager {
//    private final Bunker plugin;
//    private final ConfigUtil bunkerConfig;
//
//    public BunkerManager(Bunker plugin) {
//        this.plugin = plugin;
//        this.bunkerConfig = new ConfigUtil(plugin, "bunkers.yml");
//    }
//
//    public boolean hasBunker(String playerName) {
//        return bunkerConfig.getConfig().contains(playerName.toUpperCase());
//    }
//
//    public int getAssignedBunkers() {
//        return bunkerConfig.getConfig().getInt("assignedBunkers");
//    }
//
//    public int getTotalBunkers() {
//        return bunkerConfig.getConfig().getInt("totalBunkers");
//    }
//
//    public void setTotalBunkers(int total) {
//        bunkerConfig.getConfig().set("totalBunkers", total);
//        bunkerConfig.save();
//    }
//
//    public boolean assignBunker(String playerName) {
//        if (hasBunker(playerName)) return false;
//
//        int assigned = getAssignedBunkers();
//        int total = getTotalBunkers();
//        if (assigned >= total) return false;
//
//        bunkerConfig.getConfig().set("assignedBunkers", assigned + 1);
//        bunkerConfig.getConfig().set(playerName.toUpperCase(), assigned);
//        bunkerConfig.save();
//        return true;
//    }
//
//    public String getWorldName(String playerName) {
//        return "bunker_" + bunkerConfig.getConfig().getString(playerName.toUpperCase());
//    }
//}
//
