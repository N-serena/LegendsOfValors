package games.legendsofvalors.model.world;

import core.util.Colors;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.util.GameConfig;

import java.util.Map;

/**
 * Board Renderer - Handles map display and formatting
 */
public class BoardRenderer {

    private final LovBoard board;

    /**
     * Constructs a BoardRenderer for the specified game board
     */
    public BoardRenderer(LovBoard board) {
        this.board = board;
    }

    /**
     * Render the game board with ANSI color codes for terminal display
     * return Formatted string with ANSI colors representing the game board
     */
    public String renderColored() {
        return render(true);
    }

    /**
     * without colors
     */
    public String render() {
        return render(false);
    }

    /**
     * Rendering method shared by colored and plain rendering
     * Constructs the board grid with borders and cell contents
     * colored：If true, applies ANSI color codes; if false, plain ASCII
     * return board representation
     */
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

    /**
     * Format a single board cell with appropriate styling
     * Applies background colors, foreground colors, and padding
     * return cell string with fixed width
     */
    private String formatCell(int row, int col, boolean colored) {
        String content = cellContent(row, col, colored);
        if (!colored) {
            return pad(content);
        }
        
        String background = determineBackground(row, col);
        String foreground = determineForeground(background);
        return background + foreground + Colors.BOLD + pad(content) + Colors.RESET;
    }

    /**
     *   - Both hero and monster: "H#/M#"
     *   - Hero only: "H#"
     *   - Monster only: "M#"
     *   - Empty: terrain symbol (plain) or blank (colored)
     * return String content for the cell
     */
    private String cellContent(int row, int col, boolean colored) {
        LovBoard.CellState cell = board.getCellState(row, col);
        Map<ValorHero, String> heroLabels = board.getHeroLabels();
        Map<ValorMonster, String> monsterLabels = board.getMonsterLabels();
        
        ValorHero hero = cell.getHero();
        ValorMonster monster = cell.getMonster();
        
        boolean hasHero = hero != null;
        boolean hasMonster = monster != null;
        
        if (hasHero && hasMonster) {
            return heroLabels.get(hero) + "/" + monsterLabels.get(monster);
        }
        if (hasHero) {
            return heroLabels.get(hero);
        }
        if (hasMonster) {
            return monsterLabels.get(monster);
        }
        
        return colored ? "" : String.valueOf(board.getTile(row, col).getSymbol());
    }

    /**
     * Determine the ANSI background color for a cell
     * Based on:
     *   - Tile accessibility (inaccessible = gray)
     *   - Nexus type (monster/hero with lane color)
     *   - Terrain type (bush/cave/koulou/obstacle)
     *   - Default lane color for plain tiles
     * return ANSI background color code string
     */
    private String determineBackground(int row, int col) {
        LovTile tile = board.getTile(row, col);
        
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

    /**
     * Choose optimal foreground (text) color for readability
     * Uses black text for light backgrounds 
     * Uses white text for dark backgrounds
     * background:The ANSI background color code
     * return ANSI foreground color code (BLACK or WHITE)
     */
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

    /**
     * Get the ANSI color code for the lane containing the specified column
     * column:Column index to check
     * return ANSI color code for the lane, or white if column not in any lane
     */
    private String laneColorForColumn(int column) {
        LovBoard.Lane lane = board.getLaneForColumn(column);
        return (lane != null) ? lane.getColor() : Colors.BG_WHITE;
    }

    /**
     * Create the horizontal border line for the board grid
     */
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

    /**
     * Truncates if too long, adds spaces if too short
     */
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

    /**
     * Create a colored sample swatch for the legend display
     * Applies background color with appropriate foreground color for readability
     */
    private String colorSwatch(String background, String label) {
        return background + determineForeground(background) + pad(label) + Colors.RESET;
    }

    /**
     * Generate the complete map legend with color samples and descriptions
     * Displays:
     *   - Monster Nexus (spawn point)
     *   - Lane colors (Top/Mid/Bot)
     *   - Terrain types with stat bonuses (Bush/Cave/Koulou)
     *   - Obstacles and walls
     *   - Hero/Monster markers
     * return Formatted legend string with box-drawing characters and colors
     */
    public String getLegendText() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════════════════════════╗\n");
        sb.append("║                        Map Legend                          ║\n");
        sb.append("╠════════════════════════════════════════════════════════════╣\n");
        
        sb.append("║ ").append(colorSwatch(GameConfig.MONSTER_NEXUS_COLOR, "Nexus"))
                .append("  Monster Nexus - Spawn point for enemies            ║\n");
        
        sb.append("║ ").append(colorSwatch(GameConfig.TOP_LANE_COLOR, "Top"))
                .append("  Top Lane - Hero control zone                       ║\n");
        
        sb.append("║ ").append(colorSwatch(GameConfig.MID_LANE_COLOR, "Mid"))
                .append("  Mid Lane - Hero control zone                       ║\n");
        
        sb.append("║ ").append(colorSwatch(GameConfig.BOT_LANE_COLOR, "Bot"))
                .append("  Bot Lane - Hero control zone                       ║\n");
        
        sb.append("║ ").append(colorSwatch(GameConfig.BUSH_COLOR, "Bush"))
                .append("  Bush - Increases Dexterity                         ║\n");
        
        sb.append("║ ").append(colorSwatch(GameConfig.CAVE_COLOR, "Cave"))
                .append("  Cave - Increases Agility                           ║\n");
        
        sb.append("║ ").append(colorSwatch(GameConfig.KOULOU_COLOR, "Koulou"))
                .append("  Koulou - Increases Strength                        ║\n");
        
        sb.append("║ ").append(colorSwatch(GameConfig.OBSTACLE_COLOR, "Block"))
                .append("  Obstacle - Cleared when hero enters                ║\n");
        
        sb.append("║ ").append(colorSwatch(GameConfig.INACCESSIBLE_COLOR, "Wall"))
                .append("  Wall - Inaccessible                                ║\n");
        
        sb.append("║                                                            ║\n");
        sb.append("║ H# = Hero Marker    M# = Monster Marker                    ║\n");
        sb.append("╚════════════════════════════════════════════════════════════╝\n");
        
        return sb.toString();
    }
}
