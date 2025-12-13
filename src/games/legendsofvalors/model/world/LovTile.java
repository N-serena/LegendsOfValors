package games.legendsofvalors.model.world;

import core.model.world.Tile;

/**
 * Tile implementation for the Legends of Valor board.
 * Legends of Valor 地图的地块实现，负责地形类型、传送点标记与障碍管理。
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

    // Create a hero nexus tile.
    // 创建英雄方传送点地块。
    public static LovTile heroNexus() {
        return new LovTile(Terrain.PLAIN, NexusType.HERO, false, "HeroNexus");
    }

    // Create a monster nexus tile.
    // 创建怪物方传送点地块。
    public static LovTile monsterNexus() {
        return new LovTile(Terrain.PLAIN, NexusType.MONSTER, false, "MonsterNexus");
    }

    // Create an inaccessible wall tile.
    // 创建不可到达的墙体地块。
    public static LovTile inaccessible() {
        return new LovTile(null, NexusType.NONE, true, "Inaccessible");
    }

    // Create a regular terrain tile of the specified type.
    // 创建指定地形类型的普通地块。
    public static LovTile terrain(Terrain terrain) {
        return new LovTile(terrain, NexusType.NONE, false, terrainDisplayName(terrain));
    }

    // Check whether this tile can be entered.
    // 判断该地块是否可进入。
    public boolean isAccessible() {
        return !inaccessible;
    }

    // Determine if the tile is currently an obstacle.
    // 判断地块当前是否为障碍物。
    public boolean isObstacle() {
        return terrain == Terrain.OBSTACLE;
    }

    // Check whether this tile is the hero nexus.
    // 判断该地块是否为英雄传送点。
    public boolean isHeroNexus() {
        return nexusType == NexusType.HERO;
    }

    // Check whether this tile is the monster nexus。
    // 判断该地块是否为怪物传送点。
    public boolean isMonsterNexus() {
        return nexusType == NexusType.MONSTER;
    }

    // Get the nexus type for this tile.
    // 获取该地块的传送点类型。
    public NexusType getNexusType() {
        return nexusType;
    }

    // Get the current terrain type.
    // 返回当前地形类型。
    public Terrain getTerrain() {
        return terrain;
    }

    /**
     * Converts an obstacle into a plain tile after removal.
     * 将障碍地块清除后恢复为平原。
     */
    public void clearObstacle() {
        if (terrain == Terrain.OBSTACLE) {
            terrain = Terrain.PLAIN;
            this.type = terrainDisplayName(terrain);
        }
    }

    // Obtain the single-character symbol representing this tile.
    // 获取用于展示的单字符标记。
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
    // 判断该地块是否提供临时属性增益。
    public boolean hasTerrainBuff() {
        return terrain == Terrain.BUSH || terrain == Terrain.CAVE || terrain == Terrain.KOULOU;
    }

    // Convert the terrain enum to its display name.
    // 将地形枚举转换为显示名称。
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
        // 进入地块时的逻辑在 LoV 的棋盘层处理。
    }
}