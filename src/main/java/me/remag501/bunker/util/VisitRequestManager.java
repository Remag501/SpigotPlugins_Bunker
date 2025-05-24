package me.remag501.bunker.util;

import me.remag501.bunker.Bunker;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VisitRequestManager {
    private final Bunker plugin;
    private Map<UUID, VisitRequest> pendingRequests = new ConcurrentHashMap<>();

    public VisitRequestManager(Bunker plugin) {
        this.plugin = plugin;
    }

    public boolean hasPendingRequest(UUID bunkerOwnerUUID) {
        return pendingRequests.containsKey(bunkerOwnerUUID);
    }

    public Player getPendingRequest(UUID bunkerOwnerUUID) {
        return pendingRequests.get(bunkerOwnerUUID).getVisitor();
    }

    public void addRequest(UUID bunkerOwnerUUID, Player visitor, String worldName) {
        pendingRequests.put(bunkerOwnerUUID, new VisitRequest(visitor, worldName));
    }

    public Player removeRequest(UUID bunkerOwnerUUID) {
        return pendingRequests.remove(bunkerOwnerUUID).getVisitor();
    }

    public static class VisitRequest {
        private final Player visitor;
        private final String worldName;

        public VisitRequest(Player visitor, String worldName) {
            this.visitor = visitor;
            this.worldName = worldName;
        }

        public Player getVisitor() {
            return visitor;
        }

        public String getWorldName() {
            return worldName;
        }
    }
}
