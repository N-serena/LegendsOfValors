package games.legendsofvalors.model.world;

import core.interfaces.Board;
import core.model.entity.Monster;
import core.util.Colors;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.model.world.generator.RandomTerrainGenerator;
import games.legendsofvalors.model.world.generator.TerrainGenerator;
import games.legendsofvalors.util.GameConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Legends of Valor specific board implementation.
 * Handles tile generation, occupancy tracking, rendering, monster AI, and spawn logic.
 */
public class LovBoard implements Board {

    private static final Set<Integer> INACCESSIBLE_COLUMNS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(2, 5)));

    private final LovTile[][] tiles = new LovTile[GameConfig.BOARD_SIZE][GameConfig.BOARD_SIZE];
    private final CellState[][] occupancy = new CellState[GameConfig.BOARD_SIZE][GameConfig.BOARD_SIZE];
    private final Map<ValorHero, Position> heroPositions = new LinkedHashMap<>();
    private final Map<ValorMonster, Position> monsterPositions = new LinkedHashMap<>();
    private final Map<ValorHero, TerrainBuffRecord> activeHeroBuffs = new HashMap<>();
    private final Map<ValorHero, String> heroLabels = new LinkedHashMap<>();
    private final Map<ValorMonster, String> monsterLabels = new LinkedHashMap<>();

    private final List<Lane> lanes;
    private final Random random;
    private final TerrainGenerator terrainGenerator;

    private int heroLabelCounter;
    private int monsterLabelCounter;
    private int spawnInterval;
    private int roundsSinceLastSpawn;

    // Creates a board with a default random generator for terrain.
    public LovBoard() {
        this(new Random());
    }

    // Allows callers to supply a seeded Random for reproducible maps.
    public LovBoard(Random random) {
        this(random, new RandomTerrainGenerator());
    }

    // Full constructor used for dependency injection during testing or alternate generators.
    public LovBoard(Random random, TerrainGenerator terrainGenerator) {
        this.random = (random == null) ? new Random() : random;
        this.terrainGenerator = Objects.requireNonNull(terrainGenerator, "terrainGenerator");
        this.spawnInterval = GameConfig.DEFAULT_SPAWN_INTERVAL;
        this.roundsSinceLastSpawn = 0;
        this.lanes = initialiseLanes();
        initialiseOccupancy();
        buildBoard();
    }

    // Prepares the immutable list of board lanes and their column assignments.
    private List<Lane> initialiseLanes() {
        List<Lane> laneConfig = new ArrayList<>();
        laneConfig.add(new Lane("Top", new int[]{0, 1}, GameConfig.TOP_LANE_COLOR));
        laneConfig.add(new Lane("Mid", new int[]{3, 4}, GameConfig.MID_LANE_COLOR));
        laneConfig.add(new Lane("Bot", new int[]{6, 7}, GameConfig.BOT_LANE_COLOR));
        return Collections.unmodifiableList(laneConfig);
    }

    // Initializes cell occupancy tracking for heroes and monsters.
    private void initialiseOccupancy() {
        for (int row = 0; row < GameConfig.BOARD_SIZE; row++) {
            for (int col = 0; col < GameConfig.BOARD_SIZE; col++) {
                occupancy[row][col] = new CellState();
            }
        }
    }

    // Builds the static board layout and delegates terrain population to the generator.
    private void buildBoard() {
        layStaticCells();
        terrainGenerator.generate(tiles, lanes, random);
    }

    // Places Nexus and inaccessible tiles deterministically before terrain randomization.
    private void layStaticCells() {
        for (int row = 0; row < GameConfig.BOARD_SIZE; row++) {
            for (int col = 0; col < GameConfig.BOARD_SIZE; col++) {
                if (INACCESSIBLE_COLUMNS.contains(col)) {
                    tiles[row][col] = LovTile.inaccessible();
                    continue;
                }

                Lane lane = getLaneForColumn(col);
                if (lane == null) {
                    tiles[row][col] = LovTile.inaccessible();
                    continue;
                }

                if (row == 0) {
                    tiles[row][col] = LovTile.monsterNexus();
                } else if (row == GameConfig.BOARD_SIZE - 1) {
                    tiles[row][col] = LovTile.heroNexus();
                } else {
                    tiles[row][col] = LovTile.terrain(LovTile.Terrain.PLAIN);
                }
            }
        }

        //---MINE---//
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

    // Adjusts the monster wave spawn frequency with basic guarding.
    public void setSpawnInterval(int spawnInterval) {
        this.spawnInterval = Math.max(1, spawnInterval);
    }

    // Exposes the current spawn interval for round bookkeeping.。
    public int getSpawnInterval() {
        return spawnInterval;
    }

    @Override
    // Returns the tile at the requested coordinates after boundary validation.
    public LovTile getTile(int r, int c) {
        validateBounds(r, c);
        return tiles[r][c];
    }

    @Override
    // Board width is fixed at 8 columns for Legends of Valor.
    public int getWidth() {
        return GameConfig.BOARD_SIZE;
    }

    @Override
    // Board height is fixed at 8 rows for Legends of Valor.
    public int getHeight() {
        return GameConfig.BOARD_SIZE;
    }

    // Places or repositions a hero while applying terrain effects and clearing obstacles.
    public boolean placeHero(ValorHero hero, int row, int col) {
        Objects.requireNonNull(hero, "hero");
        if (!isPlacementValidForHero(row, col)) {
            return false;
        }
        CellState targetCell = occupancy[row][col];
        if (targetCell.hasDifferentHero(hero)) {
            return false;
        }

        registerHeroLabel(hero);
        removeTerrainBuff(hero);

        Position previous = heroPositions.remove(hero);
        if (previous != null) {
            occupancy[previous.row][previous.col].hero = null;
        }

        if (tiles[row][col].isObstacle()) {
            tiles[row][col].clearObstacle();
        }

        targetCell.hero = hero;
        //occupancy[row][col].hero = hero;
        heroPositions.put(hero, new Position(row, col));
        applyTerrainEffects(hero, tiles[row][col]);
        return true;
    }

    // Moves a hero to a new cell, enforcing lane rules and terrain buff transitions.
    public boolean moveHero(ValorHero hero, int targetRow, int targetCol) {
        Objects.requireNonNull(hero, "hero");
        Position current = heroPositions.get(hero);
        if (current == null) {
            return placeHero(hero, targetRow, targetCol);
        }
        if (!isPlacementValidForHero(targetRow, targetCol)) {
            return false;
        }
        if (!inBounds(targetRow, targetCol)) {
            return false;
        }
        CellState targetCell = occupancy[targetRow][targetCol];
        if (targetCell.hasDifferentHero(hero)) {
        //if (occupancy[targetRow][targetCol].hero != null && occupancy[targetRow][targetCol].hero != hero) {
            return false;
        }

        removeTerrainBuff(hero);
        occupancy[current.row][current.col].hero = null;

        if (tiles[targetRow][targetCol].isObstacle()) {
            tiles[targetRow][targetCol].clearObstacle();
        }

        targetCell.hero = hero;
        heroPositions.put(hero, new Position(targetRow, targetCol));
        applyTerrainEffects(hero, tiles[targetRow][targetCol]);
        return true;
    }

    // Places or repositions a monster while ensuring no conflicts with heroes.
    public boolean placeMonster(ValorMonster monster, int row, int col) {
        Objects.requireNonNull(monster, "monster");
        if (!isPlacementValidForMonster(row, col)) {
            return false;
        }
        if (!inBounds(row, col)) {
            return false;
        }
        CellState targetCell = occupancy[row][col];
        if (targetCell.hasMonster()) {
            return targetCell.monster == monster;
        }
        if (targetCell.hero != null) {
            return false;
        }

        registerMonsterLabel(monster);
        Position previous = monsterPositions.remove(monster);
        if (previous != null) {
            occupancy[previous.row][previous.col].monster = null;
        }

        targetCell.monster = monster;
        monsterPositions.put(monster, new Position(row, col));
        return true;
    }

    // Advances a monster one step forward if the lane cell is available.
    public boolean moveMonsterForward(ValorMonster monster) {
        Position current = monsterPositions.get(monster);
        if (current == null) {
            return false;
        }
        int targetRow = current.row + 1;
        if (!inBounds(targetRow, current.col)) {
            return false;
        }

        LovTile targetTile = tiles[targetRow][current.col];
        if (!targetTile.isAccessible()) {
            return false;
        }

        CellState targetCell = occupancy[targetRow][current.col];
        if (targetCell.hasMonster() || targetCell.hero != null) {
            return false;
        }

        occupancy[current.row][current.col].monster = null;
        if (targetTile.isObstacle()) {
            targetTile.clearObstacle();
        }
        targetCell.monster = monster;
        monsterPositions.put(monster, new Position(targetRow, current.col));
        return true;
    }

    // Iterates per lane to move monsters or leave them ready to attack.
    public void advanceMonsters() {
        for (Lane lane : lanes) {
                List<Map.Entry<ValorMonster, Position>> laneMonsters = monsterPositions.entrySet().stream()
                    .filter(entry -> lane.contains(entry.getValue().col))
                    .sorted(Comparator.comparingInt(entry -> -entry.getValue().row))
                    .collect(Collectors.toList());

            for (Map.Entry<ValorMonster, Position> entry : laneMonsters) {
                ValorMonster monster = entry.getKey();
                if (!getHeroesInRange(monster, 1).isEmpty()) {
                    continue; // attack instead of move
                }
                moveMonsterForward(monster);
            }
        }
    }

    // Returns heroes within the Manhattan range of a monster.
    public Set<ValorHero> getHeroesInRange(ValorMonster monster, int range) {
        Position monsterPosition = monsterPositions.get(monster);
        if (monsterPosition == null) {
            return Collections.emptySet();
        }
        int attackRange = Math.max(0, range);
        Set<ValorHero> heroes = new LinkedHashSet<>();
        for (Map.Entry<ValorHero, Position> entry : heroPositions.entrySet()) {
            if (manhattanDistance(monsterPosition, entry.getValue()) <= attackRange) {
                heroes.add(entry.getKey());
            }
        }
        return heroes;
    }

    // Returns monsters within the Manhattan range of a hero.
    // 根据曼哈顿距离返回英雄攻击范围内的怪物。
    public Set<ValorMonster> getMonstersInRange(ValorHero hero, int range) {
        Position heroPosition = heroPositions.get(hero);
        if (heroPosition == null) {
            return Collections.emptySet();
        }
        int attackRange = Math.max(0, range);
        Set<ValorMonster> monsters = new LinkedHashSet<>();
        for (Map.Entry<ValorMonster, Position> entry : monsterPositions.entrySet()) {
            if (manhattanDistance(heroPosition, entry.getValue()) <= attackRange) {
                monsters.add(entry.getKey());
            }
        }
        return monsters;
    }

    // Collects monsters that have at least one hero target in reach.
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

    // Collects heroes that have at least one monster target in reach.
    public Map<ValorHero, Set<ValorMonster>> getHeroesReadyToAttack(int range) {
        Map<ValorHero, Set<ValorMonster>> result = new LinkedHashMap<>();
        for (ValorHero hero : heroPositions.keySet()) {
            Set<ValorMonster> targets = getMonstersInRange(hero, range);
            if (!targets.isEmpty()) {
                result.put(hero, targets);
            }
        }
        return result;
    }

    // Advances the spawn counter and produces fresh monsters when the interval elapses.
    public List<ValorMonster> advanceRoundAndSpawn(List<? extends Monster> templates, int highestHeroLevel) {
        roundsSinceLastSpawn++;
        if (roundsSinceLastSpawn < spawnInterval) {
            return Collections.emptyList();
        }
        roundsSinceLastSpawn = 0;
        return spawnMonsters(templates, highestHeroLevel);
    }

    // Spawns one monster per lane using the provided templates.
    private List<ValorMonster> spawnMonsters(List<? extends Monster> templates, int highestHeroLevel) {
        if (templates == null || templates.isEmpty()) {
            return Collections.emptyList();
        }
        List<ValorMonster> spawned = new ArrayList<>();
        for (Lane lane : lanes) {
            int spawnColumn = lane.firstAvailableSpawnColumn(occupancy);
            if (spawnColumn == -1) {
                continue;
            }
            Monster template = templates.get(random.nextInt(templates.size()));
            ValorMonster monster = new ValorMonster(template, lane.getName());
            monster.scaleStats(highestHeroLevel);
            if (placeMonster(monster, 0, spawnColumn)) {
                spawned.add(monster);
            }
        }
        return spawned;
    }

    // Cleans up monster occupancy and labels after defeat.
    public void removeMonster(ValorMonster monster) {
        Position position = monsterPositions.remove(monster);
        monsterLabels.remove(monster);
        if (position == null) {
            return;
        }
        CellState cell = occupancy[position.row][position.col];
        if (cell != null) {
            cell.monster = null;
        }
    }

    // Renders an ASCII snapshot of the board without ANSI colors.
    public String render() {
        return render(false);
    }

    // Renders the board using ANSI colors for terminal UIs that support it.
    public String renderColored() {
        return render(true);
    }

    // Internal rendering helper shared by colored and uncolored output.
    private String render(boolean colored) {
        StringBuilder sb = new StringBuilder();
        String horizontalBorder = createHorizontalBorder();
        sb.append(horizontalBorder);
        for (int row = 0; row < GameConfig.BOARD_SIZE; row++) {
            sb.append('|');
            for (int col = 0; col < GameConfig.BOARD_SIZE; col++) {
                sb.append(formatCell(row, col, colored)).append('|');
            }
            sb.append(System.lineSeparator()).append(horizontalBorder);
        }
        return sb.toString();
    }

    // Provides a legend describing the color-coded map symbols.
    public String getLegendText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Map Legend (press 'L' to view in game):").append(System.lineSeparator());
        sb.append(colorSwatch(GameConfig.MONSTER_NEXUS_COLOR, "Nexus"))
                .append("  Monsters' Nexus - spawn point for enemies.").append(System.lineSeparator());
        sb.append(colorSwatch(GameConfig.TOP_LANE_COLOR, "Top"))
                .append("  Top lane path controlled by heroes.").append(System.lineSeparator());
        sb.append(colorSwatch(GameConfig.MID_LANE_COLOR, "Mid"))
                .append("  Mid lane path controlled by heroes.").append(System.lineSeparator());
        sb.append(colorSwatch(GameConfig.BOT_LANE_COLOR, "Bot"))
                .append("  Bot lane path controlled by heroes.").append(System.lineSeparator());
        sb.append(colorSwatch(GameConfig.BUSH_COLOR, "Bush"))
                .append("  Bush tile - grants Dexterity bonus while standing here.").append(System.lineSeparator());
        sb.append(colorSwatch(GameConfig.CAVE_COLOR, "Cave"))
                .append("  Cave tile - grants Agility bonus while standing here.").append(System.lineSeparator());
        sb.append(colorSwatch(GameConfig.KOULOU_COLOR, "Koulou"))
                .append("  Koulou tile - grants Strength bonus while standing here.").append(System.lineSeparator());
        sb.append(colorSwatch(GameConfig.OBSTACLE_COLOR, "Block"))
                .append("  Temporary obstacle - clears to Plain after a hero enters.").append(System.lineSeparator());
        sb.append(colorSwatch(GameConfig.INACCESSIBLE_COLOR, "Wall"))
                .append("  Inaccessible wall - cannot be entered.").append(System.lineSeparator());
        sb.append("H# / M# markers show hero or monster occupying a cell.");
        return sb.toString();
    }

    // Clears occupancy and labels so the board can be reused or restarted.
    public void reset() {
        for (int row = 0; row < GameConfig.BOARD_SIZE; row++) {
            for (int col = 0; col < GameConfig.BOARD_SIZE; col++) {
                occupancy[row][col].clear();
            }
        }
        heroPositions.clear();
        monsterPositions.clear();
        activeHeroBuffs.clear();
        heroLabels.clear();
        monsterLabels.clear();
        heroLabelCounter = 0;
        monsterLabelCounter = 0;
        roundsSinceLastSpawn = 0;
    }

    // Returns the board coordinates of the specified hero.
    public Position getHeroPosition(ValorHero hero) {
        return heroPositions.get(hero);
    }

    // Returns the board coordinates of the specified monster.
    public Position getMonsterPosition(ValorMonster monster) {
        return monsterPositions.get(monster);
    }

    // Exposes the board tile grid for read-only inspection.
    public LovTile[][] getTiles() {
        return tiles;
    }

    // Provides access to hero positions for controllers that need direct coordination.
    public Map<ValorHero, Position> getHeroPositions() {
        return heroPositions;
    }

    // Determines which lane owns a particular board column.
    public Lane getLaneForColumn(int column) {
        for (Lane lane : lanes) {
            if (lane.contains(column)) {
                return lane;
            }
        }
        return null;
    }

    // Validates whether a hero can occupy the target coordinates.
    private boolean isPlacementValidForHero(int row, int col) {
        if (!inBounds(row, col)) {
            return false;
        }
        LovTile tile = tiles[row][col];
        return tile.isAccessible() && !tile.isMonsterNexus();
    }

    // Validates whether a monster can occupy the target coordinates.
    private boolean isPlacementValidForMonster(int row, int col) {
        if (!inBounds(row, col)) {
            return false;
        }
        LovTile tile = tiles[row][col];
        return tile.isAccessible() && !tile.isHeroNexus();
    }

    // Applies terrain-specific buffs to the hero standing on the given tile.
    private void applyTerrainEffects(ValorHero hero, LovTile tile) {
        if (tile == null || !tile.hasTerrainBuff()) {
            return;
        }
        switch (tile.getTerrain()) {
            case BUSH:
                activeHeroBuffs.put(hero, TerrainBuffRecord.dexterity(hero.getDexterity()));
                hero.setDexterity(hero.getDexterity() * GameConfig.BUFF_MULTIPLIER);
                break;
            case CAVE:
                activeHeroBuffs.put(hero, TerrainBuffRecord.agility(hero.getAgility()));
                hero.setAgility(hero.getAgility() * GameConfig.BUFF_MULTIPLIER);
                break;
            case KOULOU:
                activeHeroBuffs.put(hero, TerrainBuffRecord.strength(hero.getStrength()));
                hero.setStrength(hero.getStrength() * GameConfig.BUFF_MULTIPLIER);
                break;
            default:
                // Plain/Obstacle do not grant buffs
                break;
        }
    }

    // Restores the hero's stats when leaving a terrain buff cell.
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

    // Formats the textual representation of a single board cell.
    private String formatCell(int row, int col, boolean colored) {
        String content = cellContent(row, col, colored);
        if (!colored) {
            return pad(content);
        }
        String background = determineBackground(row, col);
        String foreground = determineForeground(background);
        return background + foreground + pad(content) + Colors.RESET;
    }

    // Determines the raw symbol(s) that should appear in a cell.
    private String cellContent(int row, int col, boolean colored) {
        CellState cell = occupancy[row][col];
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
        return colored ? "" : String.valueOf(tiles[row][col].getSymbol());
    }

    // Resolves the ANSI background color for rendering a cell.
    private String determineBackground(int row, int col) {
        LovTile tile = tiles[row][col];
        if (!tile.isAccessible()) {
            return GameConfig.INACCESSIBLE_COLOR;
        }
        if (tile.isMonsterNexus()) {
            return GameConfig.MONSTER_NEXUS_COLOR;
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
                return GameConfig.BUSH_COLOR;
            case CAVE:
                return GameConfig.CAVE_COLOR;
            case KOULOU:
                return GameConfig.KOULOU_COLOR;
            case OBSTACLE:
                return GameConfig.OBSTACLE_COLOR;
            case PLAIN:
            default:
                return laneColorForColumn(col);
        }
    }

    // Selects a readable foreground color based on the background brightness.
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

    // Retrieves the lane accent color for a given column.
    private String laneColorForColumn(int column) {
        Lane lane = getLaneForColumn(column);
        return (lane != null) ? lane.getColor() : Colors.BG_WHITE;
    }

    //MINEEEE
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

    // Builds the horizontal border string reused between rows during rendering.
    private String createHorizontalBorder() {
        StringBuilder border = new StringBuilder();
        border.append('+');
        for (int col = 0; col < GameConfig.BOARD_SIZE; col++) {
            for (int i = 0; i < GameConfig.CELL_WIDTH; i++) {
                border.append('-');
            }
            border.append('+');
        }
        border.append(System.lineSeparator());
        return border.toString();
    }

    // Allocates a unique display label for a hero when first placed.
    private void registerHeroLabel(ValorHero hero) {
        heroLabels.computeIfAbsent(hero, key -> "H" + (++heroLabelCounter));
    }

    // Allocates a unique display label for a monster when first placed.
    private void registerMonsterLabel(ValorMonster monster) {
        monsterLabels.computeIfAbsent(monster, key -> "M" + (++monsterLabelCounter));
    }

    // Checks that the requested coordinates fall within the board limits.
    private boolean inBounds(int row, int col) {
        return row >= 0 && row < GameConfig.BOARD_SIZE && col >= 0 && col < GameConfig.BOARD_SIZE;
    }

    // Throws if the provided coordinates are outside the board.
    private void validateBounds(int row, int col) {
        if (!inBounds(row, col)) {
            throw new IndexOutOfBoundsException("Tile out of bounds: (" + row + ", " + col + ")");
        }
    }

    // Computes Manhattan distance for range-based interactions.
    private int manhattanDistance(Position a, Position b) {
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    // Pads content to a fixed cell width for consistent table output.
    private String pad(String raw) {
        String value = (raw == null) ? "" : raw;
        if (value.length() >= GameConfig.CELL_WIDTH) {
            return value.substring(0, GameConfig.CELL_WIDTH);
        }
        StringBuilder builder = new StringBuilder(value);
        while (builder.length() < GameConfig.CELL_WIDTH) {
            builder.append(' ');
        return builder.toString();
    }

    // Helper used by the legend to preview tile colors.
    private String colorSwatch(String background, String label) {
        return background + determineForeground(background) + pad(label) + Colors.RESET;
    }

    private static class CellState {
        private ValorHero hero;
        private ValorMonster monster;

        private boolean hasMonster() {
            return monster != null;
        }

        private boolean hasDifferentHero(ValorHero candidate) {
            return hero != null && hero != candidate;
        }

        private void clear() {
            hero = null;
            monster = null;
        }
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
        private enum BuffType {STRENGTH, DEXTERITY, AGILITY}

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

    public static class Lane {
        private final String name;
        private final int[] columns;
        private final String color;

        private Lane(String name, int[] columns, String color) {
            this.name = name;
            this.columns = Arrays.copyOf(columns, columns.length);
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public String getColor() {
            return color;
        }

        public int[] getColumns() {
            return Arrays.copyOf(columns, columns.length);
        }

        private boolean contains(int column) {
            for (int value : columns) {
                if (value == column) {
                    return true;
                }
            }
            return false;
        }

        private int firstAvailableSpawnColumn(CellState[][] occupancy) {
            for (int column : columns) {
                if (!occupancy[0][column].hasMonster()) {
                    return column;
                }
            }
            return -1;
        }
    }
}