package games.legendsofvalors.model.world;

import core.model.world.Tile;

/**
 * Tile implementation for the Legends of Valor board.
 * Handles terrain typing, nexus tagging, and obstacle management.
 */
public class LovTile extends Tile {
    public enum Terrain {
        PLAIN,
        BUSH,
        CAVE,
        KOULOU,
        OBSTACLE
    }

    public enum NexusType {
        NONE,
        HERO,
        MONSTER
    }

    private Terrain terrain;
    private final NexusType nexusType;
    private final boolean inaccessible;

    private LovTile(Terrain terrain, NexusType nexusType, boolean inaccessible, String typeName) {
        super(typeName);
        this.terrain = terrain;
        this.nexusType = nexusType;
        this.inaccessible = inaccessible;
    }

    // Create a hero nexus tile
    public static LovTile heroNexus() {
        return new LovTile(Terrain.PLAIN, NexusType.HERO, false, "HeroNexus");
    }

    // Create a monster nexus tile
    public static LovTile monsterNexus() {
        return new LovTile(Terrain.PLAIN, NexusType.MONSTER, false, "MonsterNexus");
    }

    // Create an inaccessible wall tile
    public static LovTile inaccessible() {
        return new LovTile(null, NexusType.NONE, true, "Inaccessible");
    }

    // Create a regular terrain tile of the specified type.
    public static LovTile terrain(Terrain terrain) {
        return new LovTile(terrain, NexusType.NONE, false, terrainDisplayName(terrain));
    }

    // Check whether this tile can be entered.
    public boolean isAccessible() {
        return !inaccessible;
    }

    // Determine if the tile is currently an obstacle.
    public boolean isObstacle() {
        return terrain == Terrain.OBSTACLE;
    }

    // Check whether this tile is the hero nexus.
    public boolean isHeroNexus() {
        return nexusType == NexusType.HERO;
    }

    // Check whether this tile is the monster nexus。
    public boolean isMonsterNexus() {
        return nexusType == NexusType.MONSTER;
    }

    // Get the nexus type for this tile.
    public NexusType getNexusType() {
        return nexusType;
    }

    // Get the current terrain type.
    public Terrain getTerrain() {
        return terrain;
    }

    /**
     * Converts an obstacle into a plain tile after removal.
     */
    public void clearObstacle() {
        if (terrain == Terrain.OBSTACLE) {
            terrain = Terrain.PLAIN;
            this.type = terrainDisplayName(terrain);
        }
    }

    // Obtain the single-character symbol representing this tile.
    public char getSymbol() {
        if (inaccessible) {
            return 'I';
        }
        if (nexusType != NexusType.NONE) {
            return 'N';
        }
        switch (terrain) {
            case BUSH:
                return 'B';
            case CAVE:
                return 'C';
            case KOULOU:
                return 'K';
            case OBSTACLE:
                return 'O';
            case PLAIN:
            default:
                return 'P';
        }
    }

    // Check whether the tile grants a temporary stat buff.
    public boolean hasTerrainBuff() {
        return terrain == Terrain.BUSH || terrain == Terrain.CAVE || terrain == Terrain.KOULOU;
    }

    // Convert the terrain enum to its display name.
    private static String terrainDisplayName(Terrain terrain) {
        switch (terrain) {
            case BUSH:
                return "Bush";
            case CAVE:
                return "Cave";
            case KOULOU:
                return "Koulou";
            case OBSTACLE:
                return "Obstacle";
            case PLAIN:
            default:
                return "Plain";
        }
    }

    @Override
    public void enter() {
        // Handled at the board level for LoV.
    }
}