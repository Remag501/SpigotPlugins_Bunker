package me.remag501.bunker;

import me.remag501.bunker.util.Schematic;
import org.bukkit.Location;

public class BunkerInstance {
    private final String name;
    private final Location spawnLocation;
    private final Location npcLocation;
    private final Location schematicLocation;
    private final Schematic schematic;
    private final int npcId;

    public BunkerInstance(String name, Location schematicLocation, Location spawnLocation, Location npcLocation, Schematic schematic, int npcId) {
        this.name = name;
        this.schematicLocation = schematicLocation;
        this.spawnLocation = spawnLocation;
        this.npcLocation = npcLocation;
        this.schematic = schematic;
        this.npcId = npcId;
    }

    public String getName() { return name; }
    public Location getSchematicLocation() { return schematicLocation; }
    public Location getSpawnLocation() { return spawnLocation; }
    public Location getNpcLocation() { return npcLocation; }
    public Schematic getSchematic() { return schematic; }
    public int getNpcId() { return npcId; }
}
