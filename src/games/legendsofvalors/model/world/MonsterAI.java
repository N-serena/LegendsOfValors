package games.legendsofvalors.model.world;

import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.util.GameConfig;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Includes: target selection, movement strategy, combat decisions
 */
public class MonsterAI {

    private final LovBoard board;

    /**
     * Constructs a MonsterAI instance
     * board is The game board that this AI will operate on
     */
    public MonsterAI(LovBoard board) {
        this.board = Objects.requireNonNull(board, "board");
    }

    /**
     * Priority system:
     *   1. Heroes with HP below 30% (execute low-health targets)
     *   2. Heroes with highest strength (eliminate threats)
     * return The selected target hero, or null if no valid targets
     */
    public ValorHero selectBestTarget(ValorMonster monster, Set<ValorHero> availableTargets) {
        if (availableTargets == null || availableTargets.isEmpty()) {
            return null;
        }

        // Prioritize heroes with low HP (below 30%)
        ValorHero lowestHp = availableTargets.stream()
                .filter(hero -> hero.getHealthPercentage() < 0.3)
                .min(Comparator.comparingDouble(ValorHero::getHealthPercentage))
                .orElse(null);

        if (lowestHp != null) {
            return lowestHp;
        }

        // Otherwise attack the highest threat hero (highest attack power)
        return availableTargets.stream()
                .max(Comparator.comparingDouble(ValorHero::getStrength))
                .orElse(availableTargets.iterator().next());
    }

    /**
     * Determine the optimal movement decision for a monster based on tactical analysis
     * Evaluates:
     *   - Heroes in immediate attack range (1 tile)
     *   - Nearby heroes requiring evasion (2 tiles)
     *   - Distance to both nexuses for strategic positioning
     *   - Monster's current health status
     * @param monster The monster making the decision
     * @return MovementDecision containing type (ADVANCE/RETREAT/EVADE/ATTACK/STAY) and reason
     */
    public MovementDecision makeMovementDecision(ValorMonster monster) {
        LovBoard.Position monsterPos = board.getMonsterPosition(monster);
        if (monsterPos == null) {
            return MovementDecision.stay("Monster position unknown");
        }

        // 1. Check if there are heroes in attack range
        Set<ValorHero> heroesInRange = board.getHeroesInRange(monster, 1);
        if (!heroesInRange.isEmpty()) {
            // Heroes in attack range, check if should retreat
            if (shouldRetreat(monster, heroesInRange)) {
                return MovementDecision.retreat("Low HP, retreating");
            }
            // Stay and attack
            return MovementDecision.attack("Heroes in attack range");
        }

        // 2. Check if heroes are nearby (within 2 tiles)
        Set<ValorHero> nearbyHeroes = board.getHeroesInRange(monster, 2);
        if (!nearbyHeroes.isEmpty()) {
            // Check if can evade hero attack range
            if (shouldEvadeHeroRange(monster, nearbyHeroes, monsterPos)) {
                return MovementDecision.evade("Attempting to evade hero attack range");
            }
        }

        // 3. Decide advance or retreat based on distance to bases
        int distanceToHeroNexus = GameConfig.BOARD_SIZE - 1 - monsterPos.row;
        int distanceToMonsterNexus = monsterPos.row;

        // If low HP and close to hero base, consider retreat
        if (monster.getHealthPercentage() < 0.4 && distanceToHeroNexus < 3) {
            return MovementDecision.retreat("Low HP and near enemy base");
        }

        // Default: advance
        return MovementDecision.advance("Normal advance");
    }

    /**
     * Determine if the monster should retreat based on threat assessment
     * Retreat conditions:
     *   - HP below 20% (critical health)
     *   - Surrounded by multiple heroes AND HP below 50%
     *   - Total hero threat exceeds 2x monster defense
     * @param monster The monster evaluating retreat
     * @param nearbyHeroes Set of heroes near the monster
     * @return true if monster should retreat, false otherwise
     */
    private boolean shouldRetreat(ValorMonster monster, Set<ValorHero> nearbyHeroes) {
        // Consider retreat when HP below 20%
        if (monster.getHealthPercentage() < 0.2) {
            return true;
        }

        // Surrounded by multiple heroes with HP below 50%
        if (nearbyHeroes.size() > 1 && monster.getHealthPercentage() < 0.5) {
            // Calculate total threat
            double totalThreat = nearbyHeroes.stream()
                    .mapToDouble(ValorHero::getStrength)
                    .sum();
            
            // If total hero attack far exceeds monster defense, retreat
            if (totalThreat > monster.getDefense() * 2) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determine if monster should evade to avoid hero attack range
     * Only evades if:
     *   - HP is below 60% (vulnerable)
     *   - Retreating would place monster beyond 1-tile range of all heroes
     * @param monster The monster considering evasion
     * @param nearbyHeroes Heroes within 2-tile range
     * @param currentPos Current position of the monster
     * @return true if evasion is recommended, false otherwise
     */
    private boolean shouldEvadeHeroRange(ValorMonster monster, Set<ValorHero> nearbyHeroes, LovBoard.Position currentPos) {
        // If HP is sufficient, no need to evade
        if (monster.getHealthPercentage() > 0.6) {
            return false;
        }

        // Check if can move to safe position (2+ tiles from all heroes)
        // Check if retreating would be safer
        int potentialRow = currentPos.row - 1;
        if (potentialRow >= 0) {
            LovBoard.Position backPos = new LovBoard.Position(potentialRow, currentPos.col);
            boolean wouldBeSafer = nearbyHeroes.stream()
                    .allMatch(hero -> {
                        LovBoard.Position heroPos = board.getHeroPosition(hero);
                        if (heroPos == null) return true;
                        int newDistance = Math.abs(backPos.row - heroPos.row) + Math.abs(backPos.col - heroPos.col);
                        return newDistance > 1; // Distance will be > 1 after retreat
                    });
            
            return wouldBeSafer;
        }

        return false;
    }

    /**
     * Execute the movement decision by invoking appropriate movement methods
     * Routes decision to:
     *   - ADVANCE: Move forward toward hero nexus
     *   - RETREAT: Move backward toward monster nexus
     *   - EVADE: Attempt lateral or backward movement
     *   - STAY/ATTACK: No movement
     * @param monster The monster to move
     * @param decision The movement decision to execute
     * @return true if movement was successful, false if blocked or decision was to stay
     */
    public boolean executeMovement(ValorMonster monster, MovementDecision decision) {
        LovBoard.Position currentPos = board.getMonsterPosition(monster);
        if (currentPos == null) {
            return false;
        }

        switch (decision.type) {
            case ADVANCE:
                return board.moveMonsterForward(monster);
            
            case RETREAT:
                // Try to retreat
                return tryMoveMonsterBackward(monster, currentPos);
            
            case EVADE:
                // Try lateral movement or retreat
                return tryEvadeMovement(monster, currentPos);
            
            case STAY:
            case ATTACK:
            default:
                return false; // Don't move
        }
    }

    /**
     * Attempt to move monster one row backward (toward monster nexus)
     * Validates:
     *   - Target row is within bounds (>= 0)
     *   - Target tile is accessible
     * @param monster The monster to move
     * @param current Current position of the monster
     * @return true if backward movement succeeded, false otherwise
     */
    private boolean tryMoveMonsterBackward(ValorMonster monster, LovBoard.Position current) {
        int targetRow = current.row - 1;
        if (targetRow < 0) {
            return false;
        }

        LovTile targetTile = board.getTile(targetRow, current.col);
        if (!targetTile.isAccessible()) {
            return false;
        }

        // Move monster to specified position
        return board.moveMonsterToPosition(monster, targetRow, current.col);
    }

    /**
     * Attempt evasive movement to escape hero threat
     * Strategy:
     *   1. Try backward movement first (safest)
     *   2. If blocked, try lateral movement within same lane
     * @param monster The monster attempting to evade
     * @param current Current position of the monster
     * @return true if any evasive movement succeeded, false if all attempts failed
     */
    private boolean tryEvadeMovement(ValorMonster monster, LovBoard.Position current) {
        // Try retreating first
        if (tryMoveMonsterBackward(monster, current)) {
            return true;
        }

        // If retreat fails, try lateral movement within same lane (if possible)
        LovBoard.Lane lane = board.getLaneForColumn(current.col);
        if (lane != null) {
            int[] columns = lane.getColumns();
            for (int col : columns) {
                if (col != current.col) {
                    LovTile sideTile = board.getTile(current.row, col);
                    if (sideTile.isAccessible()) {
                        return board.moveMonsterToPosition(monster, current.row, col);
                    }
                }
            }
        }

        return false;
    }

    /**
     * Movement decision class
     */
    public static class MovementDecision {
        public enum Type {
            ADVANCE,    // Move forward
            RETREAT,    // Move backward
            EVADE,      // Evade
            ATTACK,     // Stop and attack
            STAY        // Stay in place
        }

        public final Type type;
        public final String reason;

        /**
         * Private constructor for MovementDecision
         * @param type The type of movement
         * @param reason Explanation for this decision
         */
        private MovementDecision(Type type, String reason) {
            this.type = type;
            this.reason = reason;
        }

        /**
         * Create an ADVANCE decision (move toward hero nexus)
         * @param reason Explanation for advancing
         * @return MovementDecision with ADVANCE type
         */
        public static MovementDecision advance(String reason) {
            return new MovementDecision(Type.ADVANCE, reason);
        }

        /**
         * Create a RETREAT decision (move toward monster nexus)
         * @param reason Explanation for retreating
         * @return MovementDecision with RETREAT type
         */
        public static MovementDecision retreat(String reason) {
            return new MovementDecision(Type.RETREAT, reason);
        }

        /**
         * Create an EVADE decision (lateral or backward movement)
         * @param reason Explanation for evading
         * @return MovementDecision with EVADE type
         */
        public static MovementDecision evade(String reason) {
            return new MovementDecision(Type.EVADE, reason);
        }

        /**
         * Create an ATTACK decision (stay in place and attack)
         * @param reason Explanation for attacking
         * @return MovementDecision with ATTACK type
         */
        public static MovementDecision attack(String reason) {
            return new MovementDecision(Type.ATTACK, reason);
        }

        /**
         * Create a STAY decision (remain stationary)
         * @param reason Explanation for staying
         * @return MovementDecision with STAY type
         */
        public static MovementDecision stay(String reason) {
            return new MovementDecision(Type.STAY, reason);
        }
    }
}
