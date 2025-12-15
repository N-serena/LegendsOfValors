package games.legendsofvalors.model.world.generator;

import games.legendsofvalors.model.world.LovBoard;
import games.legendsofvalors.model.world.LovTile;
import games.legendsofvalors.util.GameConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Default terrain generator that produces a balanced random spread of special tiles
 * Ensures all terrain types appear at least once and distributes them fairly across lanes
 */
public class RandomTerrainGenerator implements TerrainGenerator {

    /**
     * Populate the inner lane tiles with randomized terrain while preserving mandatory types
     * Strategy:
     *   1. Collect all inner lane positions (rows 1-6)
     *   2. Shuffle positions randomly
     *   3. Place one of each required type (BUSH, CAVE, KOULOU, OBSTACLE)
     *   4. Fill remaining positions with weighted random terrain
     *   5. Ensure at least one PLAIN tile exists
     * tiles: is the2D array of tiles to populate
     * lanes: List of lane definitions with column mappings
     * random:Random number generator for reproducible terrain
     */
    @Override
    public void generate(LovTile[][] tiles, List<LovBoard.Lane> lanes, Random random) {
        Objects.requireNonNull(tiles, "tiles");
        Objects.requireNonNull(lanes, "lanes");
        Random rng = (random == null) ? new Random() : random;

        if (tiles.length == 0) {
            return;
        }

        List<LovBoard.Position> innerPositions = collectInnerLanePositions(tiles.length, lanes);
        if (innerPositions.isEmpty()) {
            return;
        }

        Collections.shuffle(innerPositions, rng);

        LovTile.Terrain[] required = new LovTile.Terrain[]{
                LovTile.Terrain.BUSH,
                LovTile.Terrain.CAVE,
                LovTile.Terrain.KOULOU,
                LovTile.Terrain.OBSTACLE
        };

        int index = 0;
        for (; index < required.length && index < innerPositions.size(); index++) {
            LovBoard.Position position = innerPositions.get(index);
            tiles[position.row][position.col] = LovTile.terrain(required[index]);
        }

        boolean hasPlain = false;
        for (; index < innerPositions.size(); index++) {
            LovBoard.Position position = innerPositions.get(index);
            LovTile.Terrain terrain = rollTerrain(rng);
            tiles[position.row][position.col] = LovTile.terrain(terrain);
            if (terrain == LovTile.Terrain.PLAIN) {
                hasPlain = true;
            }
        }

        if (!hasPlain) {
            LovBoard.Position fallback = innerPositions.get(innerPositions.size() - 1);
            tiles[fallback.row][fallback.col] = LovTile.terrain(LovTile.Terrain.PLAIN);
        }
    }

    /**
     * Collect the coordinates inside each lane that are eligible for random terrain placement
     * Excludes row 0 (monster nexus) and row 7 (hero nexus)
     * return List of Position objects representing inner lane cells
     */
    private List<LovBoard.Position> collectInnerLanePositions(int boardSize, List<LovBoard.Lane> lanes) {
        List<LovBoard.Position> positions = new ArrayList<>();
        for (int row = 1; row < boardSize - 1; row++) {
            for (LovBoard.Lane lane : lanes) {
                for (int column : lane.getColumns()) {
                    positions.add(new LovBoard.Position(row, column));
                }
            }
        }
        return positions;
    }

    /**
     * Roll a weighted random terrain type based on the configured probabilities
     * Uses cumulative probability distribution from GameConfig weights
     * Default distribution: PLAIN (55%), BUSH (15%), CAVE (15%), KOULOU (10%), OBSTACLE (5%)
     * @param random Random number generator
     * @return Randomly selected Terrain type based on weights
     */
    private LovTile.Terrain rollTerrain(Random random) {
        double roll = random.nextDouble();
        if (roll < GameConfig.PLAIN_WEIGHT) {
            return LovTile.Terrain.PLAIN;
        }
        if (roll < GameConfig.PLAIN_WEIGHT + GameConfig.BUSH_WEIGHT) {
            return LovTile.Terrain.BUSH;
        }
        if (roll < GameConfig.PLAIN_WEIGHT + GameConfig.BUSH_WEIGHT + GameConfig.CAVE_WEIGHT) {
            return LovTile.Terrain.CAVE;
        }
        if (roll < GameConfig.PLAIN_WEIGHT + GameConfig.BUSH_WEIGHT + GameConfig.CAVE_WEIGHT + GameConfig.KOULOU_WEIGHT) {
            return LovTile.Terrain.KOULOU;
        }
        return LovTile.Terrain.OBSTACLE;
    }
}
