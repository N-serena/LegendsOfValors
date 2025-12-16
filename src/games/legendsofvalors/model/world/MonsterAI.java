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
    private static final double RISK_WEIGHT = 1.4;
    private static final double PROGRESS_WEIGHT = 2.2;
    private static final double ATTACK_OPPORTUNITY_BONUS = 4.0;
    private static final double OBSTACLE_PENALTY = 0.6;
    private static final double MIN_SCORE_IMPROVEMENT = 0.35;
    private static final double ATTACK_RISK_THRESHOLD = 1.2;
    private static final double FORCED_RETREAT_RISK_THRESHOLD = 1.65;

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

        LovBoard.Position monsterPos = board.getMonsterPosition(monster);
        Map<ValorHero, LovBoard.Position> heroPositions = board.getHeroPositions();

        Comparator<ValorHero> byPriority = Comparator.comparingDouble(hero ->
            -evaluateTargetPriority(monster, hero, monsterPos, heroPositions.get(hero)));

        return availableTargets.stream()
            .sorted(byPriority.thenComparing(ValorHero::getStrength, Comparator.reverseOrder()))
            .findFirst()
            .orElse(availableTargets.iterator().next());
    }

    /**
     * Determine the optimal movement decision for a monster based on tactical analysis
     * Evaluates:
     *   - Heroes in immediate attack range (1 tile)
     *   - Nearby heroes requiring evasion (2 tiles)
     *   - Distance to both nexuses for strategic positioning
     *   - Monster's current health status
     * return MovementDecision type (ADVANCE/RETREAT/EVADE/ATTACK/STAY)
     */
    public MovementDecision makeMovementDecision(ValorMonster monster) {
        LovBoard.Position monsterPos = board.getMonsterPosition(monster);
        if (monsterPos == null) {
            return MovementDecision.stay("Monster position unknown");
        }

        LovBoard.Lane monsterLane = board.getLaneForColumn(monsterPos.col);
        Map<ValorHero, LovBoard.Position> heroPositions = getHeroesInLane(monsterLane);

        if (heroPositions.isEmpty()) {
            if (isAdvanceAvailable(monsterPos)) {
                LovBoard.Position forward = new LovBoard.Position(monsterPos.row + 1, monsterPos.col);
                return MovementDecision.advance("No heroes detected, marching forward", forward);
            }
            return MovementDecision.stay("No heroes detected and lane blocked");
        }

        Set<ValorHero> heroesInRange = board.getHeroesInRange(monster, 1);
        if (!heroesInRange.isEmpty()) {
            if (shouldRetreat(monster, heroesInRange, heroPositions)) {
                if (isRetreatAvailable(monsterPos)) {
                    LovBoard.Position retreatPos = new LovBoard.Position(monsterPos.row - 1, monsterPos.col);
                    return MovementDecision.retreat("Overwhelmed in melee, falling back", retreatPos);
                }
                if (hasEvadeRoute(monsterPos)) {
                    Optional<LovBoard.Position> escape = findBestEvadeSpot(monster, monsterPos, heroPositions);
                    return MovementDecision.evade("Retreat blocked, attempting lateral escape", escape.orElse(null));
                }
                return MovementDecision.attack("Cornered, forced to strike back");
            }

            double currentRisk = evaluatePositionRisk(monster, monsterPos, heroPositions);
            if (currentRisk <= ATTACK_RISK_THRESHOLD) {
                return MovementDecision.attack("Favorable melee exchange");
            }
        }

        Set<ValorHero> nearbyHeroes = board.getHeroesInRange(monster, 2);
        if (!nearbyHeroes.isEmpty() && shouldEvadeHeroRange(monster, nearbyHeroes, monsterPos, heroPositions)) {
            Optional<LovBoard.Position> escape = findBestEvadeSpot(monster, monsterPos, heroPositions);
            if (escape.isPresent()) {
                return MovementDecision.evade("Seeking safer distance", escape.get());
            }
        }

        int currentDistance = GameConfig.BOARD_SIZE - 1 - monsterPos.row;
        double stayScore = scoreCandidate(monster, monsterPos, monsterPos, heroPositions, currentDistance);

        List<CandidateMove> candidates = new ArrayList<>();
        candidates.add(new CandidateMove(MovementDecision.Type.STAY, stayScore, monsterPos, "Holding formation"));

        evaluateAdvanceCandidate(monster, monsterPos, heroPositions, currentDistance)
                .ifPresent(candidates::add);
        evaluateRetreatCandidate(monster, monsterPos, heroPositions, currentDistance)
                .ifPresent(candidates::add);
        evaluateLateralCandidates(monster, monsterPos, heroPositions, currentDistance)
                .ifPresent(candidates::add);

        CandidateMove bestMove = candidates.stream()
                .max(Comparator.comparingDouble(move -> move.score))
                .orElse(new CandidateMove(MovementDecision.Type.STAY, stayScore, monsterPos, "Holding formation"));

        double improvement = bestMove.score - stayScore;
        if (bestMove.type == MovementDecision.Type.STAY || improvement < MIN_SCORE_IMPROVEMENT) {
            if (!heroesInRange.isEmpty()) {
                return MovementDecision.attack("Minimal benefit from repositioning");
            }
            return MovementDecision.stay(bestMove.reason);
        }

        switch (bestMove.type) {
            case ADVANCE:
                return MovementDecision.advance(bestMove.reason, bestMove.targetPosition);
            case RETREAT:
                return MovementDecision.retreat(bestMove.reason, bestMove.targetPosition);
            case EVADE:
                return MovementDecision.evade(bestMove.reason, bestMove.targetPosition);
            default:
                return MovementDecision.stay(bestMove.reason);
        }
    }

    /**
     * Determine if the monster should retreat based on threat assessment
     * Retreat conditions:
     *   - HP below 20% (critical health)
     *   - Surrounded by multiple heroes AND HP below 50%
     *   - Total hero threat exceeds 2x monster defense
     * return true if monster should retreat, false otherwise
     */
    private boolean shouldRetreat(ValorMonster monster, Set<ValorHero> nearbyHeroes,
                                  Map<ValorHero, LovBoard.Position> laneHeroPositions) {
        LovBoard.Position position = board.getMonsterPosition(monster);

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

        if (position != null) {
            double risk = evaluatePositionRisk(monster, position, laneHeroPositions);
            if (risk > FORCED_RETREAT_RISK_THRESHOLD) {
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
     * return true if evasion is recommended, false otherwise
     */
    private boolean shouldEvadeHeroRange(ValorMonster monster, Set<ValorHero> nearbyHeroes,
                                         LovBoard.Position currentPos,
                                         Map<ValorHero, LovBoard.Position> laneHeroPositions) {
        if (monster.getHealthPercentage() > 0.6 || nearbyHeroes.isEmpty()) {
            return false;
        }

        double currentRisk = evaluatePositionRisk(monster, currentPos, laneHeroPositions);

        List<LovBoard.Position> escapeCandidates = new ArrayList<>();
        if (isRetreatAvailable(currentPos)) {
            escapeCandidates.add(new LovBoard.Position(currentPos.row - 1, currentPos.col));
        }
        escapeCandidates.addAll(getLateralPositions(currentPos));

        for (LovBoard.Position candidate : escapeCandidates) {
            double candidateRisk = evaluatePositionRisk(monster, candidate, laneHeroPositions);
            if (candidateRisk + 0.2 < currentRisk) {
                return true;
            }
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
     * return true if movement was successful, false if blocked or decision was to stay
     */
    public boolean executeMovement(ValorMonster monster, MovementDecision decision) {
        LovBoard.Position currentPos = board.getMonsterPosition(monster);
        if (currentPos == null) {
            return false;
        }

        switch (decision.type) {
            case ADVANCE:
                if (decision.targetPosition != null) {
                    return board.moveMonsterToPosition(monster, decision.targetPosition.row, decision.targetPosition.col);
                }
                return board.moveMonsterForward(monster);
            
            case RETREAT:
                if (decision.targetPosition != null) {
                    return board.moveMonsterToPosition(monster, decision.targetPosition.row, decision.targetPosition.col);
                }
                return tryMoveMonsterBackward(monster, currentPos);
            
            case EVADE:
                if (decision.targetPosition != null) {
                    return board.moveMonsterToPosition(monster, decision.targetPosition.row, decision.targetPosition.col);
                }
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
     * return true if backward movement succeeded, false otherwise
     */
    private boolean tryMoveMonsterBackward(ValorMonster monster, LovBoard.Position current) {
        int targetRow = current.row - 1;
        if (!isTraversable(targetRow, current.col)) {
            return false;
        }

        return board.moveMonsterToPosition(monster, targetRow, current.col);
    }

    /**
     * Attempt evasive movement to escape hero threat
     * Strategy:
     *   1. Try backward movement first (safest)
     *   2. If blocked, try lateral movement within same lane
     * return true if any evasive movement succeeded, false if all attempts failed
     */
    private boolean tryEvadeMovement(ValorMonster monster, LovBoard.Position current) {
        if (tryMoveMonsterBackward(monster, current)) {
            return true;
        }

        for (LovBoard.Position lateral : getLateralPositions(current)) {
            if (board.moveMonsterToPosition(monster, lateral.row, lateral.col)) {
                return true;
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
        public final LovBoard.Position targetPosition;

        /**
         * Private constructor for MovementDecision
         */
        private MovementDecision(Type type, String reason, LovBoard.Position targetPosition) {
            this.type = type;
            this.reason = reason;
            this.targetPosition = targetPosition;
        }

        /**
         * Create an ADVANCE decision (move toward hero nexus)
         */
        public static MovementDecision advance(String reason) {
            return new MovementDecision(Type.ADVANCE, reason, null);
        }

        public static MovementDecision advance(String reason, LovBoard.Position target) {
            return new MovementDecision(Type.ADVANCE, reason, target);
        }

        /**
         * Create a RETREAT decision (move toward monster nexus)
         */
        public static MovementDecision retreat(String reason) {
            return new MovementDecision(Type.RETREAT, reason, null);
        }

        public static MovementDecision retreat(String reason, LovBoard.Position target) {
            return new MovementDecision(Type.RETREAT, reason, target);
        }

        /**
         * Create an EVADE decision (lateral or backward movement)
         */
        public static MovementDecision evade(String reason) {
            return new MovementDecision(Type.EVADE, reason, null);
        }

        public static MovementDecision evade(String reason, LovBoard.Position target) {
            return new MovementDecision(Type.EVADE, reason, target);
        }

        /**
         * Create an ATTACK decision (stay in place and attack)
         */
        public static MovementDecision attack(String reason) {
            return new MovementDecision(Type.ATTACK, reason, null);
        }

        /**
         * Create a STAY decision (remain stationary)
         */
        public static MovementDecision stay(String reason) {
            return new MovementDecision(Type.STAY, reason, null);
        }
    }

    /**
     * Evaluate moving forward (toward hero nexus) as a candidate move
     * Checks if the forward tile is traversable and calculates movement score
     * return Optional containing CandidateMove if advance is possible, empty otherwise
     */
    private Optional<CandidateMove> evaluateAdvanceCandidate(ValorMonster monster, LovBoard.Position current,
                                                              Map<ValorHero, LovBoard.Position> heroPositions,
                                                              int currentDistance) {
        int targetRow = current.row + 1;
        int targetCol = current.col;
        if (!isTraversable(targetRow, targetCol)) {
            return Optional.empty();
        }

        LovBoard.Position candidate = new LovBoard.Position(targetRow, targetCol);
        double score = scoreCandidate(monster, current, candidate, heroPositions, currentDistance);
        return Optional.of(new CandidateMove(MovementDecision.Type.ADVANCE, score, candidate, "Advancing toward nexus"));
    }

    /**
     * Evaluate moving backward (toward monster nexus) as a candidate move
     * Checks if the backward tile is traversable and calculates movement score
     * return Optional containing CandidateMove if retreat is possible, empty otherwise
     */
    private Optional<CandidateMove> evaluateRetreatCandidate(ValorMonster monster, LovBoard.Position current,
                                                              Map<ValorHero, LovBoard.Position> heroPositions,
                                                              int currentDistance) {
        int targetRow = current.row - 1;
        int targetCol = current.col;
        if (!isTraversable(targetRow, targetCol)) {
            return Optional.empty();
        }

        LovBoard.Position candidate = new LovBoard.Position(targetRow, targetCol);
        double score = scoreCandidate(monster, current, candidate, heroPositions, currentDistance);
        return Optional.of(new CandidateMove(MovementDecision.Type.RETREAT, score, candidate, "Falling back to reduce threat"));
    }

    /**
     * Evaluate lateral movement (left/right within same lane) as candidate moves
     * Scores all available lateral positions and returns the best option
     * return Optional containing best lateral CandidateMove, empty if no lateral moves available
     */
    private Optional<CandidateMove> evaluateLateralCandidates(ValorMonster monster, LovBoard.Position current,
                                                               Map<ValorHero, LovBoard.Position> heroPositions,
                                                               int currentDistance) {
        List<LovBoard.Position> lateralPositions = getLateralPositions(current);
        if (lateralPositions.isEmpty()) {
            return Optional.empty();
        }

        return lateralPositions.stream()
                .map(candidate -> {
                    double score = scoreCandidate(monster, current, candidate, heroPositions, currentDistance);
                    return new CandidateMove(MovementDecision.Type.EVADE, score, candidate, "Shifting laterally to balance risk");
                })
                .max(Comparator.comparingDouble(move -> move.score));
    }

    /**
     * Check if advancing forward one row is possible
     * return true if the tile one row ahead is traversable, false otherwise
     */
    private boolean isAdvanceAvailable(LovBoard.Position current) {
        return current != null && isTraversable(current.row + 1, current.col);
    }

    /**
     * Check if retreating backward one row is possible
     * return true if the tile one row behind is traversable, false otherwise
     */
    private boolean isRetreatAvailable(LovBoard.Position current) {
        return current != null && isTraversable(current.row - 1, current.col);
    }

    /**
     * Check if any evasive movement options exist (backward or lateral)
     * return true if retreat or lateral movement is available, false if blocked
     */
    private boolean hasEvadeRoute(LovBoard.Position current) {
        if (current == null) {
            return false;
        }
        return isRetreatAvailable(current) || !getLateralPositions(current).isEmpty();
    }

    /**
     * Find the safest position to evade to by evaluating all escape routes
     * Considers both backward and lateral movement options
     * return Optional containing position with lowest risk, empty if no escape routes available
     */
    private Optional<LovBoard.Position> findBestEvadeSpot(ValorMonster monster, LovBoard.Position current,
                                                           Map<ValorHero, LovBoard.Position> heroPositions) {
        List<LovBoard.Position> candidates = new ArrayList<>();
        if (isRetreatAvailable(current)) {
            candidates.add(new LovBoard.Position(current.row - 1, current.col));
        }
        candidates.addAll(getLateralPositions(current));

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        return candidates.stream()
                .min(Comparator.comparingDouble(position -> evaluatePositionRisk(monster, position, heroPositions)));
    }

    /**
     * Get all traversable lateral positions within the same lane
     * Lateral positions are other columns in the same lane at the current row
     * return List of traversable lateral positions, empty if none available
     */
    private List<LovBoard.Position> getLateralPositions(LovBoard.Position current) {
        if (current == null) {
            return Collections.emptyList();
        }
        LovBoard.Lane lane = board.getLaneForColumn(current.col);
        if (lane == null) {
            return Collections.emptyList();
        }

        List<LovBoard.Position> positions = new ArrayList<>();
        for (int col : lane.getColumns()) {
            if (col == current.col) {
                continue;
            }
            if (isTraversable(current.row, col)) {
                positions.add(new LovBoard.Position(current.row, col));
            }
        }
        return positions;
    }

    /**
     * Check if a tile at the specified position is traversable for monsters
     * Validates:
     *   - Position is within board bounds
    *   - Tile is accessible
     *   - Cell is not occupied by another hero or monster
     * return true if the tile can be traversed, false otherwise
     */
    private boolean isTraversable(int row, int col) {
        if (row < 0 || col < 0 || row >= GameConfig.BOARD_SIZE || col >= GameConfig.BOARD_SIZE) {
            return false;
        }

        LovTile tile = board.getTile(row, col);
        if (!tile.isAccessible()) {
            return false;
        }

        LovBoard.CellState state = board.getCellState(row, col);
        return state.getHero() == null && state.getMonster() == null;
    }

    /**
     * Calculate tactical score for a candidate movement position
     * Factors considered:
     *   - Progress toward hero nexus (weighted by health)
     *   - Attack opportunities (heroes in range)
     *   - Position risk (proximity to heroes)
     *   - Obstacle penalties
     * return Score value, higher is better
     */
    private double scoreCandidate(ValorMonster monster, LovBoard.Position origin, LovBoard.Position candidate,
                                  Map<ValorHero, LovBoard.Position> heroPositions, int currentDistance) {
        int candidateDistance = GameConfig.BOARD_SIZE - 1 - candidate.row;
        int distanceDelta = currentDistance - candidateDistance;

        double healthFactor = 0.5 + monster.getHealthPercentage();
        double progressScore = distanceDelta * PROGRESS_WEIGHT * healthFactor;
        if (distanceDelta < 0) {
            progressScore *= 0.65;
        }

        double risk = evaluatePositionRisk(monster, candidate, heroPositions);
        int targetsInRange = countTargetsInRange(candidate, heroPositions, 1);
        double attackScore = targetsInRange * ATTACK_OPPORTUNITY_BONUS
                * (monster.getBaseDamage() / Math.max(10.0, monster.getDefense()));

        LovTile tile = board.getTile(candidate.row, candidate.col);
        double obstaclePenalty = tile.isObstacle() ? OBSTACLE_PENALTY : 0.0;

        return progressScore + attackScore - (risk * RISK_WEIGHT) - obstaclePenalty;
    }

    /**
     * Evaluate the danger level of a position based on nearby heroes
     * Risk factors:
     *   - Hero strength, agility, and level
     *   - Distance to each hero (closer = more dangerous)
     *   - Proximity bonus for adjacent heroes
     *   - Normalized by monster's defense
     *   - Modified by monster's current health
     * return Risk score, higher values indicate more danger
     */
    private double evaluatePositionRisk(ValorMonster monster, LovBoard.Position candidate,
                                        Map<ValorHero, LovBoard.Position> heroPositions) {
        double combinedThreat = 0.0;
        double defense = Math.max(1.0, monster.getDefense());

        for (Map.Entry<ValorHero, LovBoard.Position> entry : heroPositions.entrySet()) {
            LovBoard.Position heroPos = entry.getValue();
            if (heroPos == null) {
                continue;
            }

            int distance = manhattanDistance(candidate, heroPos);
            double baseThreat = entry.getKey().getStrength() * 0.7
                    + entry.getKey().getAgility() * 0.3
                    + entry.getKey().getLevel() * 2.0;

            double distanceFactor = 1.0 / Math.max(1, distance);
            double proximityBonus = (distance <= 1) ? 1.5 : (distance == 2 ? 1.1 : 1.0);
            combinedThreat += baseThreat * distanceFactor * proximityBonus;
        }

        double normalized = combinedThreat / defense;
        double healthModifier = 1.0 + (1.0 - monster.getHealthPercentage());
        return normalized * healthModifier;
    }

    private Map<ValorHero, LovBoard.Position> getHeroesInLane(LovBoard.Lane lane) {
        if (lane == null) {
            return Collections.emptyMap();
        }

        int[] laneColumns = lane.getColumns();
        Map<ValorHero, LovBoard.Position> filtered = new LinkedHashMap<>();
        for (Map.Entry<ValorHero, LovBoard.Position> entry : board.getHeroPositions().entrySet()) {
            LovBoard.Position position = entry.getValue();
            if (position == null) {
                continue;
            }
            if (containsColumn(laneColumns, position.col)) {
                filtered.put(entry.getKey(), position);
            }
        }
        return filtered;
    }

    private boolean containsColumn(int[] columns, int column) {
        for (int value : columns) {
            if (value == column) {
                return true;
            }
        }
        return false;
    }

    /**
     * Count the number of heroes within attack range of a position
     * Uses Manhattan distance for range calculation
     * return Number of heroes within specified range
     */
    private int countTargetsInRange(LovBoard.Position candidate,
                                    Map<ValorHero, LovBoard.Position> heroPositions, int range) {
        int count = 0;
        for (LovBoard.Position heroPos : heroPositions.values()) {
            if (heroPos == null) {
                continue;
            }
            if (manhattanDistance(candidate, heroPos) <= range) {
                count++;
            }
        }
        return count;
    }

    /**
     * Calculate priority score for targeting a specific hero
     * Higher scores indicate better targets
     * Factors:
     *   - Hero vulnerability (low health prioritized)
     *   - Distance to hero (closer is better)
     *   - Threat level (strong heroes prioritized)
     *   - Agility mitigation (reduces priority for evasive heroes)
     * return Priority score for targeting this hero
     */
    private double evaluateTargetPriority(ValorMonster monster, ValorHero hero,
                                          LovBoard.Position monsterPos, LovBoard.Position heroPos) {
        double healthPct = Math.max(0.0, Math.min(1.0, hero.getHealthPercentage()));
        double vulnerability = (1.0 - healthPct) * 2.5;
        if (healthPct < 0.3) {
            vulnerability += 1.2;
        }

        double distanceFactor = 0.5;
        if (monsterPos != null && heroPos != null) {
            int distance = manhattanDistance(monsterPos, heroPos);
            distanceFactor = 1.0 / (1.0 + distance);
        }

        double threatLevel = hero.getStrength() / Math.max(1.0, monster.getDefense());
        double agilityMitigation = hero.getAgility() / 250.0;

        return vulnerability + (threatLevel * (1.5 + distanceFactor)) + distanceFactor - agilityMitigation;
    }

    /**
     * Calculate Manhattan distance between two positions
     * Manhattan distance = |row1 - row2| + |col1 - col2|
     * return Distance in tiles
     */
    private int manhattanDistance(LovBoard.Position a, LovBoard.Position b) {
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    /**
     * Internal class representing a candidate movement option with its tactical score
     * Used for comparing and selecting the best movement decision
     */
    private static final class CandidateMove {
        private final MovementDecision.Type type;
        private final double score;
        private final LovBoard.Position targetPosition;
        private final String reason;

        private CandidateMove(MovementDecision.Type type, double score, LovBoard.Position targetPosition, String reason) {
            this.type = type;
            this.score = score;
            this.targetPosition = targetPosition;
            this.reason = reason;
        }
    }
}
