package me.remag501.bunker.util;

import org.bukkit.entity.Player;

public class VisitRequest {
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
