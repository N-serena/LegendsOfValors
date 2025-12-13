package games.legendsofvalors.model.world;

import core.interfaces.Board;
import core.model.entity.Monster;
import core.util.Colors;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;


public class LovBoard implements Board {
    public static final int BOARD_SIZE = 8;
    public static final int DEFAULT_SPAWN_INTERVAL = 8;
    private static final double BUFF_MULTIPLIER = 1.1;
    private static final int CELL_WIDTH = 6;

    private static final String TOP_LANE_COLOR = Colors.BG_BRIGHT_BLUE;
    private static final String MID_LANE_COLOR = Colors.BG_BRIGHT_GREEN;
    private static final String BOT_LANE_COLOR = Colors.BG_BRIGHT_YELLOW;

    private static final String MONSTER_NEXUS_COLOR = Colors.BG_RED;
    private static final String INACCESSIBLE_COLOR = Colors.BG_BLACK;
    private static final String BUSH_COLOR = Colors.BG_GREEN;
    private static final String CAVE_COLOR = Colors.BG_PURPLE;
    private static final String KOULOU_COLOR = Colors.BG_BRIGHT_WHITE;
    private static final String OBSTACLE_COLOR = Colors.BG_BRIGHT_PURPLE;

    private final LovTile[][] tiles;
    private final CellOccupancy[][] occupancy;
    private final Map<ValorHero, Position> heroPositions;
    private final Map<ValorMonster, Position> monsterPositions;
    private final Map<ValorHero, TerrainBuffRecord> activeHeroBuffs;
    private final Map<ValorHero, String> heroLabels;
    private final Map<ValorMonster, String> monsterLabels;
    private int heroLabelCounter;
    private int monsterLabelCounter;

    private final List<Lane> lanes;
    private final Random random;
    private int spawnInterval;
    private int roundsSinceLastSpawn;

    public LovBoard() {
        this(new Random());
    }

    public LovBoard(Random random) {
        this.tiles = new LovTile[BOARD_SIZE][BOARD_SIZE];
        this.occupancy = new CellOccupancy[BOARD_SIZE][BOARD_SIZE];
        this.heroPositions = new LinkedHashMap<>();
        this.monsterPositions = new LinkedHashMap<>();
        this.activeHeroBuffs = new HashMap<>();
        this.heroLabels = new LinkedHashMap<>();
        this.monsterLabels = new LinkedHashMap<>();
        this.random = (random == null) ? new Random() : random;
        this.spawnInterval = DEFAULT_SPAWN_INTERVAL;
        this.roundsSinceLastSpawn = 0;

        List<Lane> laneConfig = new ArrayList<Lane>();
        laneConfig.add(new Lane("Top", new int[]{0, 1}, TOP_LANE_COLOR));
        laneConfig.add(new Lane("Mid", new int[]{3, 4}, MID_LANE_COLOR));
        laneConfig.add(new Lane("Bot", new int[]{6, 7}, BOT_LANE_COLOR));
        this.lanes = Collections.unmodifiableList(laneConfig);

        initialiseOccupancy();
        buildBoard();
    }

    private void initialiseOccupancy() {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                occupancy[r][c] = new CellOccupancy();
            }
        }
    }

    private void buildBoard() {
        // 1. Lay down static structures (lanes, nexus, inaccessible columns).
        Set<Integer> laneColumns = lanes.stream()
            .flatMapToInt(lane -> Arrays.stream(lane.columns))
                .boxed()
                .collect(Collectors.toSet());
        Set<Integer> inaccessibleColumns = new HashSet<>();
        inaccessibleColumns.add(2);
        inaccessibleColumns.add(5);

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (inaccessibleColumns.contains(col)) {
                    tiles[row][col] = LovTile.inaccessible();
                    continue;
                }

                if (!laneColumns.contains(col)) {
                    tiles[row][col] = LovTile.inaccessible();
                    continue;
                }

                if (row == 0) {
                    tiles[row][col] = LovTile.monsterNexus();
                } else if (row == BOARD_SIZE - 1) {
                    tiles[row][col] = LovTile.heroNexus();
                } else {
                    tiles[row][col] = LovTile.terrain(LovTile.Terrain.PLAIN);
                }
            }
        }

        // 2. Randomise lane terrain for inner rows (1..6).
        List<Position> available = new ArrayList<>();
        for (int row = 1; row < BOARD_SIZE - 1; row++) {
            for (Lane lane : lanes) {
                for (int col : lane.columns) {
                    available.add(new Position(row, col));
                }
            }
        }
        Collections.shuffle(available, random);

        LovTile.Terrain[] required = new LovTile.Terrain[]{
                LovTile.Terrain.BUSH,
                LovTile.Terrain.CAVE,
                LovTile.Terrain.KOULOU,
                LovTile.Terrain.OBSTACLE
        };

        int index = 0;
        for (; index < required.length && index < available.size(); index++) {
            Position pos = available.get(index);
            tiles[pos.row][pos.col] = LovTile.terrain(required[index]);
        }

        for (; index < available.size(); index++) {
            Position pos = available.get(index);
            LovTile.Terrain terrain = randomTerrain();
            tiles[pos.row][pos.col] = LovTile.terrain(terrain);
        }
    }

    private LovTile.Terrain randomTerrain() {
        // Weighted random distribution favouring Plain tiles.
        double roll = random.nextDouble();
        if (roll < 0.55) {
            return LovTile.Terrain.PLAIN;
        } else if (roll < 0.7) {
            return LovTile.Terrain.BUSH;
        } else if (roll < 0.85) {
            return LovTile.Terrain.CAVE;
        } else if (roll < 0.95) {
            return LovTile.Terrain.KOULOU;
        }
        return LovTile.Terrain.OBSTACLE;
    }

    public void setSpawnInterval(int spawnInterval) {
        this.spawnInterval = Math.max(1, spawnInterval);
    }

    public int getSpawnInterval() {
        return spawnInterval;
    }

    @Override
    public LovTile getTile(int r, int c) {
        validateBounds(r, c);
        return tiles[r][c];
    }

    @Override
    public int getWidth() {
        return BOARD_SIZE;
    }

    @Override
    public int getHeight() {
        return BOARD_SIZE;
    }

    public boolean placeHero(ValorHero hero, int row, int col) {
        Objects.requireNonNull(hero, "hero");
        if (!isPlacementValidForHero(row, col)) {
            return false;
        }
        registerHeroLabel(hero);
        removeTerrainBuff(hero);
        Position previous = heroPositions.get(hero);
        if (previous != null) {
            occupancy[previous.row][previous.col].hero = null;
        }
        occupancy[row][col].hero = hero;
        heroPositions.put(hero, new Position(row, col));
        applyTerrainEffects(hero, tiles[row][col]);
        return true;
    }

    public boolean moveHero(ValorHero hero, int targetRow, int targetCol) {
        Objects.requireNonNull(hero, "hero");
        Position current = heroPositions.get(hero);
        if (current == null) {
            return placeHero(hero, targetRow, targetCol);
        }
        if (!isPlacementValidForHero(targetRow, targetCol)) {
            return false;
        }
        if (occupancy[targetRow][targetCol].hero != null && occupancy[targetRow][targetCol].hero != hero) {
            return false;
        }

        removeTerrainBuff(hero);
        occupancy[current.row][current.col].hero = null;
        occupancy[targetRow][targetCol].hero = hero;
        heroPositions.put(hero, new Position(targetRow, targetCol));

        LovTile targetTile = tiles[targetRow][targetCol];
        if (targetTile.isObstacle()) {
            targetTile.clearObstacle();
        }
        applyTerrainEffects(hero, targetTile);
        return true;
    }

    public boolean placeMonster(ValorMonster monster, int row, int col) {
        Objects.requireNonNull(monster, "monster");
        if (!isPlacementValidForMonster(row, col)) {
            return false;
        }
        registerMonsterLabel(monster);
        Position previous = monsterPositions.get(monster);
        if (previous != null) {
            occupancy[previous.row][previous.col].monster = null;
        }
        if (occupancy[row][col].monster != null && occupancy[row][col].monster != monster) {
            return false;
        }
        occupancy[row][col].monster = monster;
        monsterPositions.put(monster, new Position(row, col));
        return true;
    }

    public boolean moveMonsterForward(ValorMonster monster) {
        Position current = monsterPositions.get(monster);
        if (current == null) {
            return false;
        }
        int targetRow = current.row + 1;
        if (targetRow >= BOARD_SIZE) {
            return false;
        }
        LovTile targetTile = tiles[targetRow][current.col];
        if (!targetTile.isAccessible()) {
            return false;
        }
        CellOccupancy targetCell = occupancy[targetRow][current.col];
        if (targetCell.hero != null) {
            return false;
        }
        if (targetCell.monster != null) {
            return false;
        }
        if (targetTile.isObstacle()) {
            targetTile.clearObstacle();
        }
        occupancy[current.row][current.col].monster = null;
        occupancy[targetRow][current.col].monster = monster;
        monsterPositions.put(monster, new Position(targetRow, current.col));
        return true;
    }

    public void advanceMonsters() {
        for (Lane lane : lanes) {
            List<ValorMonster> laneMonsters = monsterPositions.entrySet().stream()
                    .filter(entry -> lane.contains(entry.getValue().col))
                    .sorted((a, b) -> Integer.compare(b.getValue().row, a.getValue().row))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            for (ValorMonster monster : laneMonsters) {
                if (!getHeroesInRange(monster, 1).isEmpty()) {
                    continue;
                }
                moveMonsterForward(monster);
            }
        }
    }

    public Set<ValorHero> getHeroesInRange(ValorMonster monster, int range) {
        Position monsterPosition = monsterPositions.get(monster);
        if (monsterPosition == null) {
            return Collections.emptySet();
        }
        int attackRange = Math.max(0, range);
        return heroPositions.entrySet().stream()
                .filter(entry -> manhattanDistance(monsterPosition, entry.getValue()) <= attackRange)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    public Map<ValorMonster, Set<ValorHero>> getMonstersReadyToAttack(int range) {
        Map<ValorMonster, Set<ValorHero>> result = new LinkedHashMap<>();
        for (ValorMonster monster : monsterPositions.keySet()) {
            Set<ValorHero> targets = getHeroesInRange(monster, range);
            if (!targets.isEmpty()) {
                result.put(monster, targets);
            }
        }
        return result;
    }

    public List<ValorMonster> advanceRoundAndSpawn(List<? extends Monster> templates, int highestHeroLevel) {
        roundsSinceLastSpawn++;
        if (roundsSinceLastSpawn < spawnInterval) {
            return Collections.emptyList();
        }
        roundsSinceLastSpawn = 0;
        return spawnMonsters(templates, highestHeroLevel);
    }

    private List<ValorMonster> spawnMonsters(List<? extends Monster> templates, int highestHeroLevel) {
        if (templates == null || templates.isEmpty()) {
            return Collections.emptyList();
        }
        List<ValorMonster> spawns = new ArrayList<>();
        for (Lane lane : lanes) {
            int spawnColumn = lane.firstAvailableSpawnColumn();
            if (spawnColumn == -1) {
                continue;
            }
            Monster template = templates.get(random.nextInt(templates.size()));
            ValorMonster monster = new ValorMonster(template, lane.name);
            monster.scaleStats(highestHeroLevel);
            if (placeMonster(monster, 0, spawnColumn)) {
                spawns.add(monster);
            }
        }
        return spawns;
    }

    public String render() {
        return render(false);
    }

    public String renderColored() {
        return render(true);
    }

    private String render(boolean colored) {
        StringBuilder sb = new StringBuilder();
        String horizontalBorder = createHorizontalBorder();
        sb.append(horizontalBorder);
        for (int row = 0; row < BOARD_SIZE; row++) {
            sb.append('|');
            for (int col = 0; col < BOARD_SIZE; col++) {
                sb.append(formatCell(row, col, colored));
                sb.append('|');
            }
            sb.append(System.lineSeparator());
            sb.append(horizontalBorder);
        }
        return sb.toString();
    }

    public String getLegendText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Map Legend (press 'L' to view in game):").append(System.lineSeparator());
        sb.append(colorSwatch(MONSTER_NEXUS_COLOR, "Nexus")).append("  Monsters' Nexus - spawn point for enemies.").append(System.lineSeparator());
        sb.append(colorSwatch(TOP_LANE_COLOR, "Top")).append("  Top lane path controlled by heroes.").append(System.lineSeparator());
        sb.append(colorSwatch(MID_LANE_COLOR, "Mid")).append("  Mid lane path controlled by heroes.").append(System.lineSeparator());
        sb.append(colorSwatch(BOT_LANE_COLOR, "Bot")).append("  Bot lane path controlled by heroes.").append(System.lineSeparator());
        sb.append(colorSwatch(BUSH_COLOR, "Bush")).append("  Bush tile - grants Dexterity bonus while standing here.").append(System.lineSeparator());
        sb.append(colorSwatch(CAVE_COLOR, "Cave")).append("  Cave tile - grants Agility bonus while standing here.").append(System.lineSeparator());
        sb.append(colorSwatch(KOULOU_COLOR, "Koulou")).append("  Koulou tile - grants Strength bonus while standing here.").append(System.lineSeparator());
        sb.append(colorSwatch(OBSTACLE_COLOR, "Block")).append("  Temporary obstacle - clears to Plain after a hero enters.").append(System.lineSeparator());
        sb.append(colorSwatch(INACCESSIBLE_COLOR, "Wall")).append("  Inaccessible wall - cannot be entered.").append(System.lineSeparator());
        sb.append("H# / M# markers show hero or monster occupying a cell.");
        return sb.toString();
    }

    public void reset() {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                occupancy[r][c].hero = null;
                occupancy[r][c].monster = null;
            }
        }
        heroPositions.clear();
        monsterPositions.clear();
        activeHeroBuffs.clear();
        roundsSinceLastSpawn = 0;
    }

    public Position getHeroPosition(ValorHero hero) {
        return heroPositions.get(hero);
    }

    public Position getMonsterPosition(ValorMonster monster) {
        return monsterPositions.get(monster);
    }

    public Lane getLaneForColumn(int column) {
        for (Lane lane : lanes) {
            if (lane.contains(column)) {
                return lane;
            }
        }
        return null;
    }

    private boolean isPlacementValidForHero(int row, int col) {
        if (!inBounds(row, col)) {
            return false;
        }
        LovTile tile = tiles[row][col];
        return tile.isAccessible() && !tile.isMonsterNexus();
    }

    private boolean isPlacementValidForMonster(int row, int col) {
        if (!inBounds(row, col)) {
            return false;
        }
        LovTile tile = tiles[row][col];
        return tile.isAccessible() && !tile.isHeroNexus();
    }

    private void applyTerrainEffects(ValorHero hero, LovTile tile) {
        if (tile == null) {
            return;
        }
        if (!tile.hasTerrainBuff()) {
            return;
        }
        switch (tile.getTerrain()) {
            case BUSH:
                activeHeroBuffs.put(hero, TerrainBuffRecord.dexterity(hero.getDexterity()));
                hero.setDexterity(hero.getDexterity() * BUFF_MULTIPLIER);
                break;
            case CAVE:
                activeHeroBuffs.put(hero, TerrainBuffRecord.agility(hero.getAgility()));
                hero.setAgility(hero.getAgility() * BUFF_MULTIPLIER);
                break;
            case KOULOU:
                activeHeroBuffs.put(hero, TerrainBuffRecord.strength(hero.getStrength()));
                hero.setStrength(hero.getStrength() * BUFF_MULTIPLIER);
                break;
            default:
                // No buff
                break;
        }
    }

    private void removeTerrainBuff(ValorHero hero) {
        TerrainBuffRecord record = activeHeroBuffs.remove(hero);
        if (record == null) {
            return;
        }
        switch (record.buffType) {
            case STRENGTH:
                hero.setStrength(record.originalValue);
                break;
            case DEXTERITY:
                hero.setDexterity(record.originalValue);
                break;
            case AGILITY:
                hero.setAgility(record.originalValue);
                break;
            default:
                break;
        }
    }

    private String formatCell(int row, int col, boolean colored) {
        String content = cellContent(row, col, colored);
        if (!colored) {
            return pad(content);
        }
        String background = determineBackground(row, col);
        String foreground = determineForeground(background);
        return background + foreground + pad(content) + Colors.RESET;
    }

    private String cellContent(int row, int col, boolean colored) {
        CellOccupancy cell = occupancy[row][col];
        boolean hasHero = cell.hero != null;
        boolean hasMonster = cell.monster != null;
        if (hasHero && hasMonster) {
            return heroLabels.get(cell.hero) + "/" + monsterLabels.get(cell.monster);
        }
        if (hasHero) {
            return heroLabels.get(cell.hero);
        }
        if (hasMonster) {
            return monsterLabels.get(cell.monster);
        }
        if (colored) {
            return "";
        }
        return String.valueOf(tiles[row][col].getSymbol());
    }

    private String determineBackground(int row, int col) {
        LovTile tile = tiles[row][col];
        if (!tile.isAccessible()) {
            return INACCESSIBLE_COLOR;
        }
        if (tile.isMonsterNexus()) {
            return MONSTER_NEXUS_COLOR;
        }
        if (tile.isHeroNexus()) {
            return laneColorForColumn(col);
        }
        LovTile.Terrain terrain = tile.getTerrain();
        if (terrain == null) {
            return laneColorForColumn(col);
        }
        switch (terrain) {
            case BUSH:
                return BUSH_COLOR;
            case CAVE:
                return CAVE_COLOR;
            case KOULOU:
                return KOULOU_COLOR;
            case OBSTACLE:
                return OBSTACLE_COLOR;
            case PLAIN:
            default:
                return laneColorForColumn(col);
        }
    }

    private String determineForeground(String background) {
        if (background.equals(Colors.BG_YELLOW)
                || background.equals(Colors.BG_BRIGHT_YELLOW)
                || background.equals(Colors.BG_WHITE)
                || background.equals(Colors.BG_BRIGHT_WHITE)
                || background.equals(Colors.BG_CYAN)
                || background.equals(Colors.BG_BRIGHT_CYAN)
                || background.equals(Colors.BG_BRIGHT_GREEN)) {
            return Colors.BLACK;
        }
        return Colors.WHITE;
    }

    private String laneColorForColumn(int column) {
        Lane lane = getLaneForColumn(column);
        if (lane != null) {
            return lane.color;
        }
        return Colors.BG_WHITE;
    }

    public ValorMonster getLeadingMonsterInLane(int column) {
        Lane lane = getLaneForColumn(column);
        if (lane == null) return null;

        ValorMonster leader = null;
        int maxRow = -1;

        // Iterate through all monsters to find the one in this lane with the highest row index
        for (Map.Entry<ValorMonster, Position> entry : monsterPositions.entrySet()) {
            // Check if monster is in the same lane (by checking if the lane contains the monster's col)
            if (lane.contains(entry.getValue().col)) {
                if (entry.getValue().row > maxRow) {
                    maxRow = entry.getValue().row;
                    leader = entry.getKey();
                }
            }
        }
        return leader;
    }

    // Helper for TeleportCommand: Checks if a cell is blocked
    public boolean isCellBlocked(int r, int c) {
        if (!inBounds(r, c)) return true;
        if (!tiles[r][c].isAccessible()) return true;
        // Check occupancy
        if (occupancy[r][c].hero != null || occupancy[r][c].monster != null) return true;
        return false;
    }

    private String createHorizontalBorder() {
        StringBuilder border = new StringBuilder();
        border.append('+');
        for (int col = 0; col < BOARD_SIZE; col++) {
            for (int i = 0; i < CELL_WIDTH; i++) {
                border.append('-');
            }
            border.append('+');
        }
        border.append(System.lineSeparator());
        return border.toString();
    }

    private void registerHeroLabel(ValorHero hero) {
        heroLabels.computeIfAbsent(hero, h -> "H" + (++heroLabelCounter));
    }

    private void registerMonsterLabel(ValorMonster monster) {
        monsterLabels.computeIfAbsent(monster, m -> "M" + (++monsterLabelCounter));
    }

    private boolean inBounds(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private void validateBounds(int row, int col) {
        if (!inBounds(row, col)) {
            throw new IndexOutOfBoundsException("Tile out of bounds: (" + row + ", " + col + ")");
        }
    }

    private int manhattanDistance(Position a, Position b) {
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    private static String pad(String raw) {
        if (raw == null) {
            raw = "";
        }
        if (raw.length() >= CELL_WIDTH) {
            return raw.substring(0, CELL_WIDTH);
        }
        StringBuilder sb = new StringBuilder(raw);
        while (sb.length() < CELL_WIDTH) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private String colorSwatch(String background, String label) {
        return background + determineForeground(background) + pad(label) + Colors.RESET;
    }

    private class Lane {
        private final String name;
        private final int[] columns;
        private final String color;

        private Lane(String name, int[] columns, String color) {
            this.name = name;
            this.columns = columns;
            this.color = color;
        }

        private boolean contains(int column) {
            for (int c : columns) {
                if (c == column) {
                    return true;
                }
            }
            return false;
        }

        private int firstAvailableSpawnColumn() {
            for (int column : columns) {
                if (occupancy[0][column].monster == null) {
                    return column;
                }
            }
            return -1;
        }
    }

    private static class CellOccupancy {
        private ValorHero hero;
        private ValorMonster monster;
    }

    public static class Position {
        public final int row;
        public final int col;

        public Position(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    private static class TerrainBuffRecord {
        private enum BuffType { STRENGTH, DEXTERITY, AGILITY }

        private final BuffType buffType;
        private final double originalValue;

        private TerrainBuffRecord(BuffType buffType, double originalValue) {
            this.buffType = buffType;
            this.originalValue = originalValue;
        }

        private static TerrainBuffRecord strength(double original) {
            return new TerrainBuffRecord(BuffType.STRENGTH, original);
        }

        private static TerrainBuffRecord dexterity(double original) {
            return new TerrainBuffRecord(BuffType.DEXTERITY, original);
        }

        private static TerrainBuffRecord agility(double original) {
            return new TerrainBuffRecord(BuffType.AGILITY, original);
        }
    }
}