package me.remag501.bunker.core;

import me.remag501.bunker.util.SchematicUtil;
import org.bukkit.Location;

public class BunkerInstance {
    private final String name;
    private final Location spawnLocation;
    private final Location npcLocation;
    private final Location schematicLocation;
    private final SchematicUtil schematic;
    private final int npcId;
    private final Location generatorLocation;

    private final String generatorType;
    private final String hologramName;
    private final Location hologramLocation;

    public BunkerInstance(String name, Location schematicLocation, Location spawnLocation, Location npcLocation,
                          SchematicUtil schematic, Location generatorLocation, String generatorType, int npcId, String hologramName, Location hologramLocation) {
        this.name = name;
        this.schematicLocation = schematicLocation;
        this.spawnLocation = spawnLocation;
        this.npcLocation = npcLocation;
        this.schematic = schematic;
        this.npcId = npcId;
        this.generatorLocation = generatorLocation;
        this.generatorType = generatorType;
        this.hologramName = hologramName;
        this.hologramLocation = hologramLocation;
    }

    public String getName() { return name; }
    public Location getSchematicLocation() { return schematicLocation; }
    public Location getSpawnLocation() { return spawnLocation; }
    public Location getNpcLocation() { return npcLocation; }
    public SchematicUtil getSchematic() { return schematic; }
    public int getNpcId() { return npcId; }
    public Location getGeneratorLocation() {return generatorLocation;}
    public String getGeneratorType() {return generatorType;}
    public String getHologramName() {return hologramName;}
    public Location getHologramLocation() {return hologramLocation;}

}
