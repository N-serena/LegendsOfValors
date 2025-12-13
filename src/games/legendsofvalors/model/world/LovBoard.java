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
    // 创建一个使用默认随机生成器的地图。
    public LovBoard() {
        this(new Random());
    }

    // Allows callers to supply a seeded Random for reproducible maps.
    // 允许调用方传入带种子的 Random，以便复现地图布局。
    public LovBoard(Random random) {
        this(random, new RandomTerrainGenerator());
    }

    // Full constructor used for dependency injection during testing or alternate generators.
    // 完整构造函数，可注入自定义生成器或测试用依赖。
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
    // 准备不可变的线路配置及其列索引。
    private List<Lane> initialiseLanes() {
        List<Lane> laneConfig = new ArrayList<>();
        laneConfig.add(new Lane("Top", new int[]{0, 1}, GameConfig.TOP_LANE_COLOR));
        laneConfig.add(new Lane("Mid", new int[]{3, 4}, GameConfig.MID_LANE_COLOR));
        laneConfig.add(new Lane("Bot", new int[]{6, 7}, GameConfig.BOT_LANE_COLOR));
        return Collections.unmodifiableList(laneConfig);
    }

    // Initializes cell occupancy tracking for heroes and monsters.
    // 初始化格子占用状态，记录英雄与怪物。
    private void initialiseOccupancy() {
        for (int row = 0; row < GameConfig.BOARD_SIZE; row++) {
            for (int col = 0; col < GameConfig.BOARD_SIZE; col++) {
                occupancy[row][col] = new CellState();
            }
        }
    }

    // Builds the static board layout and delegates terrain population to the generator.
    // 构建静态结构，并交给地形生成器填充随机地形。
    private void buildBoard() {
        layStaticCells();
        terrainGenerator.generate(tiles, lanes, random);
    }

    // Places Nexus and inaccessible tiles deterministically before terrain randomization.
    // 在随机化前放置固定的 Nexus 与不可达格。
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
    }

    // Adjusts the monster wave spawn frequency with basic guarding.
    // 调整怪物刷新的回合间隔，保证最小值为 1。
    public void setSpawnInterval(int spawnInterval) {
        this.spawnInterval = Math.max(1, spawnInterval);
    }

    // Exposes the current spawn interval for round bookkeeping.
    // 返回当前的刷怪间隔，供回合逻辑使用。
    public int getSpawnInterval() {
        return spawnInterval;
    }

    @Override
    // Returns the tile at the requested coordinates after boundary validation.
    // 在检查边界后返回对应坐标的地块。
    public LovTile getTile(int r, int c) {
        validateBounds(r, c);
        return tiles[r][c];
    }

    @Override
    // Board width is fixed at 8 columns for Legends of Valor.
    // 地图宽度固定为 8 列。
    public int getWidth() {
        return GameConfig.BOARD_SIZE;
    }

    @Override
    // Board height is fixed at 8 rows for Legends of Valor.
    // 地图高度固定为 8 行。
    public int getHeight() {
        return GameConfig.BOARD_SIZE;
    }

    // Places or repositions a hero while applying terrain effects and clearing obstacles.
    // 放置或移动英雄，处理地形增益与障碍清除。
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
        heroPositions.put(hero, new Position(row, col));
        applyTerrainEffects(hero, tiles[row][col]);
        return true;
    }

    // Moves a hero to a new cell, enforcing lane rules and terrain buff transitions.
    // 移动英雄到目标格，同时处理线路限制与地形增益切换。
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
    // 放置或移动怪物，确保不会与英雄冲突。
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
    // 若前方格子可用，则让怪物前进一步。
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
    // 按线路处理怪物：若有目标则留在原地准备攻击，否则尝试前进。
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
    // 根据曼哈顿距离返回怪物攻击范围内的英雄。
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
    // 收集范围内至少有一个英雄可攻击的怪物。
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
    // 收集范围内至少有一个怪物目标的英雄。
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
    // 推进回合计数，到达间隔时刷出新怪物。
    public List<ValorMonster> advanceRoundAndSpawn(List<? extends Monster> templates, int highestHeroLevel) {
        roundsSinceLastSpawn++;
        if (roundsSinceLastSpawn < spawnInterval) {
            return Collections.emptyList();
        }
        roundsSinceLastSpawn = 0;
        return spawnMonsters(templates, highestHeroLevel);
    }

    // Spawns one monster per lane using the provided templates.
    // 使用模板在每条线路上生成一只怪物。
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
    // 怪物被击败后清理其占用状态与显示标签。
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
    // 以 ASCII 形式渲染地图（无颜色）。
    public String render() {
        return render(false);
    }

    // Renders the board using ANSI colors for terminal UIs that support it.
    // 使用 ANSI 颜色渲染地图，适用于支持彩色终端。
    public String renderColored() {
        return render(true);
    }

    // Internal rendering helper shared by colored and uncolored output.
    // 彩色与非彩色渲染共用的内部方法。
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
    // 输出地图符号与颜色说明。
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
    // 清空占用状态与标签，以便重新开始或复用地图。
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
    // 返回指定英雄的坐标。
    public Position getHeroPosition(ValorHero hero) {
        return heroPositions.get(hero);
    }

    // Returns the board coordinates of the specified monster.
    // 返回指定怪物的坐标。
    public Position getMonsterPosition(ValorMonster monster) {
        return monsterPositions.get(monster);
    }

    // Exposes the board tile grid for read-only inspection.
    // 提供只读访问的地块数组。
    public LovTile[][] getTiles() {
        return tiles;
    }

    // Provides access to hero positions for controllers that need direct coordination.
    // 返回英雄位置映射，供控制器直接使用。
    public Map<ValorHero, Position> getHeroPositions() {
        return heroPositions;
    }

    // Determines which lane owns a particular board column.
    // 判断某列所属的线路。
    public Lane getLaneForColumn(int column) {
        for (Lane lane : lanes) {
            if (lane.contains(column)) {
                return lane;
            }
        }
        return null;
    }

    // Validates whether a hero can occupy the target coordinates.
    // 检查英雄是否允许占据目标坐标。
    private boolean isPlacementValidForHero(int row, int col) {
        if (!inBounds(row, col)) {
            return false;
        }
        LovTile tile = tiles[row][col];
        return tile.isAccessible() && !tile.isMonsterNexus();
    }

    // Validates whether a monster can occupy the target coordinates.
    // 检查怪物是否允许占据目标坐标。
    private boolean isPlacementValidForMonster(int row, int col) {
        if (!inBounds(row, col)) {
            return false;
        }
        LovTile tile = tiles[row][col];
        return tile.isAccessible() && !tile.isHeroNexus();
    }

    // Applies terrain-specific buffs to the hero standing on the given tile.
    // 为站在特殊地形上的英雄施加对应的属性增益。
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
    // 英雄离开增益地形时恢复原始属性。
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
    // 格式化单个格子的文本渲染。
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
    // 决定格子中显示的符号或标签。
    private String cellContent(int row, int col, boolean colored) {
        CellState cell = occupancy[row][col];
        if (cell.hero != null && cell.monster != null) {
            return heroLabels.get(cell.hero) + "/" + monsterLabels.get(cell.monster);
        }
        if (cell.hero != null) {
            return heroLabels.get(cell.hero);
        }
        if (cell.monster != null) {
            return monsterLabels.get(cell.monster);
        }
        return colored ? "" : String.valueOf(tiles[row][col].getSymbol());
    }

    // Resolves the ANSI background color for rendering a cell.
    // 根据地形判定格子的背景颜色。
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
    // 根据背景亮度选择适合的前景色。
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
    // 返回某列对应线路的背景颜色。
    private String laneColorForColumn(int column) {
        Lane lane = getLaneForColumn(column);
        return (lane != null) ? lane.getColor() : Colors.BG_WHITE;
    }

    // Builds the horizontal border string reused between rows during rendering.
    // 构建渲染表格时使用的水平边框。
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
    // 为初次出现的英雄生成唯一标签。
    private void registerHeroLabel(ValorHero hero) {
        heroLabels.computeIfAbsent(hero, key -> "H" + (++heroLabelCounter));
    }

    // Allocates a unique display label for a monster when first placed.
    // 为初次出现的怪物生成唯一标签。
    private void registerMonsterLabel(ValorMonster monster) {
        monsterLabels.computeIfAbsent(monster, key -> "M" + (++monsterLabelCounter));
    }

    // Checks that the requested coordinates fall within the board limits.
    // 判断坐标是否处于地图范围内。
    private boolean inBounds(int row, int col) {
        return row >= 0 && row < GameConfig.BOARD_SIZE && col >= 0 && col < GameConfig.BOARD_SIZE;
    }

    // Throws if the provided coordinates are outside the board.
    // 坐标越界时抛出异常。
    private void validateBounds(int row, int col) {
        if (!inBounds(row, col)) {
            throw new IndexOutOfBoundsException("Tile out of bounds: (" + row + ", " + col + ")");
        }
    }

    // Computes Manhattan distance for range-based interactions.
    // 计算曼哈顿距离，用于范围判定。
    private int manhattanDistance(Position a, Position b) {
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    // Pads content to a fixed cell width for consistent table output.
    // 将内容填充到固定宽度，确保表格对齐。
    private String pad(String raw) {
        String value = (raw == null) ? "" : raw;
        if (value.length() >= GameConfig.CELL_WIDTH) {
            return value.substring(0, GameConfig.CELL_WIDTH);
        }
        StringBuilder builder = new StringBuilder(value);
        while (builder.length() < GameConfig.CELL_WIDTH) {
            builder.append(' ');
        }
        return builder.toString();
    }

    // Helper used by the legend to preview tile colors.
    // 供图例展示颜色示意的辅助方法。
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
