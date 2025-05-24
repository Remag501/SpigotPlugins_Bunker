package me.remag501.bunker.util;

import me.remag501.bunker.Bunker;

import java.util.UUID;

public class VisitRequestManager {
    private final Bunker plugin;

    public VisitRequestManager(Bunker plugin) {
        this.plugin = plugin;
    }

    public boolean hasPendingRequest(UUID bunkerOwnerUUID) {
        return plugin.getPendingRequests().containsKey(bunkerOwnerUUID);
    }

    public VisitRequest getPendingRequest(UUID bunkerOwnerUUID) {
        return plugin.getPendingRequests().get(bunkerOwnerUUID);
    }

    public void addRequest(UUID bunkerOwnerUUID, VisitRequest request) {
        plugin.getPendingRequests().put(bunkerOwnerUUID, request);
    }

    public VisitRequest removeRequest(UUID bunkerOwnerUUID) {
        return plugin.getPendingRequests().remove(bunkerOwnerUUID);
    }
}

