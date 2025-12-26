package me.remag501.bunker.core;

import me.remag501.bunker.util.SchematicUtil;
import org.bukkit.Location;

import java.util.List;

public class BunkerInstance {
    private final String name;
    private final List<SchematicWrapper> schematics;
    private final List<NPCInfo> npcs;
    private final List<GeneratorInfo> generators;
    private final List<HologramInfo> holograms;
    private final List<String> removeHolograms;

    public BunkerInstance(String name,
                          List<SchematicWrapper> schematics,
                          List<NPCInfo> npcs,
                          List<GeneratorInfo> generators,
                          List<HologramInfo> holograms,
                          List<String> removeHolograms) {
        this.name = name;
        this.schematics = schematics;
        this.npcs = npcs;
        this.generators = generators;
        this.holograms = holograms;
        this.removeHolograms = removeHolograms;
    }

    public String getName() { return name; }
    public List<SchematicWrapper> getSchematics() { return schematics; }
    public List<NPCInfo> getNpcs() { return npcs; }
    public List<GeneratorInfo> getGenerators() { return generators; }
    public List<HologramInfo> getHolograms() { return holograms; }
    public List<String> getRemoveHolograms () {return removeHolograms; }

    // Helper data classes
    public static class NPCInfo {
        public int id;
        public Location location;

        public NPCInfo(int id, Location location) {
            this.id = id;
            this.location = location;
        }
    }

    public static class GeneratorInfo {
        public String type;
        public int level;
        public Location location;

        public GeneratorInfo(String type, Location location) {
            this.type = type;
            this.location = location;
        }
    }

    public static class HologramInfo {
        public String name;
        public String type;
        public Location location;

        public HologramInfo(String name, String type, Location location) {
            this.name = name;
            this.type = type;
            this.location = location;
        }
    }

    public static class SchematicWrapper {
        public SchematicUtil schematic;
        public Location location;

        public SchematicWrapper(SchematicUtil schematic, Location location) {
            this.schematic = schematic;
            this.location = location;
        }
    }
}

