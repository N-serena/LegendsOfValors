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

    public static LovTile heroNexus() {
        return new LovTile(Terrain.PLAIN, NexusType.HERO, false, "HeroNexus");
    }

    public static LovTile monsterNexus() {
        return new LovTile(Terrain.PLAIN, NexusType.MONSTER, false, "MonsterNexus");
    }

    public static LovTile inaccessible() {
        return new LovTile(null, NexusType.NONE, true, "Inaccessible");
    }

    public static LovTile terrain(Terrain terrain) {
        return new LovTile(terrain, NexusType.NONE, false, terrainDisplayName(terrain));
    }

    public boolean isAccessible() {
        return !inaccessible;
    }

    public boolean isObstacle() {
        return terrain == Terrain.OBSTACLE;
    }

    public boolean isHeroNexus() {
        return nexusType == NexusType.HERO;
    }

    public boolean isMonsterNexus() {
        return nexusType == NexusType.MONSTER;
    }

    public NexusType getNexusType() {
        return nexusType;
    }

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

    public boolean hasTerrainBuff() {
        return terrain == Terrain.BUSH || terrain == Terrain.CAVE || terrain == Terrain.KOULOU;
    }

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