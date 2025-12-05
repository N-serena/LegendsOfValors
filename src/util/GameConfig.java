package util;

/**
 * GameConfig class holds all the constant configuration values for the RPG game.
 * This includes board dimensions, level-up scaling constants, combat constants,
 * world generation probabilities, and other game mechanics settings.
 ** @author Serena N
 * @version 3.0 (Refactored for scalability and maintainability)
 */
public class GameConfig {
    // Board Dimensions (Scalable!)
    public static final int BOARD_WIDTH = 8;  // Default to 8 as per spec
    public static final int BOARD_HEIGHT = 8;

    // --- LEVEL UP SCALING CONSTANTS ---
    public static final double HP_PER_LEVEL = 100.0;
    public static final double MANA_SCALE = 1.1;
    public static final double SKILL_SCALE = 1.05;
    public static final double MANA_BASE_FLOOR = 100.0;

    // --- COMBAT CONSTANTS ---
    public static final double DODGE_SCALE = 0.002;
    public static final double DAMAGE_SCALE = 0.05;

    // World Generation
    public static final double CHANCE_WALL = 0.20;
    public static final double CHANCE_MARKET = 0.30;

    // Combat Balance
    public static final double MAX_DODGE_CHANCE = 0.50; // Cap at 50%
    public static final double HERO_HP_SCALING = 100.0;
    public static final double REGEN_RATE = 1.1; // 10% regen
    public static final double REVIVE_RATE = 50; // 50%

    // Mercenary Guild Settings
    public static final int GUILD_COST_PER_LEVEL = 500;
    public static final int GUILD_MAX_SWAPS = 1; // Scarcity rule

    // Exploration
    public static final double CHANCE_LOOT = 0.10; // 10% chance for treasure
    public static final double CHANCE_BATTLE = 0.60; // 50% chance for fight

    // Limits
    public static final int MAX_LEVEL = 10;
}