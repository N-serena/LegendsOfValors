package games.legendsofvalors.model.world.generator;

import games.legendsofvalors.model.world.LovBoard;
import games.legendsofvalors.model.world.LovTile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Default terrain generator that produces a balanced random spread of special tiles.
 * 默认地形生成器，保证特殊地形在地图上合理分布。
 */
public class RandomTerrainGenerator implements TerrainGenerator {
    private static final double PLAIN_WEIGHT = 0.55;
    private static final double BUSH_WEIGHT = 0.15;
    private static final double CAVE_WEIGHT = 0.15;
    private static final double KOULOU_WEIGHT = 0.1;

    @Override
    // Populate the inner lane tiles with randomized terrain while preserving mandatory types.
    // 为各线路的内部格子生成随机地形，并确保必备地形类型出现。
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

    // Collect the coordinates inside each lane that are eligible for random terrain placement.
    // 获取各条线路中可用来随机化地形的内部坐标。
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

    // Roll a weighted random terrain type based on the configured probabilities.
    // 根据设定的权重生成一个随机地形类型。
    private LovTile.Terrain rollTerrain(Random random) {
        double roll = random.nextDouble();
        if (roll < PLAIN_WEIGHT) {
            return LovTile.Terrain.PLAIN;
        }
        if (roll < PLAIN_WEIGHT + BUSH_WEIGHT) {
            return LovTile.Terrain.BUSH;
        }
        if (roll < PLAIN_WEIGHT + BUSH_WEIGHT + CAVE_WEIGHT) {
            return LovTile.Terrain.CAVE;
        }
        if (roll < PLAIN_WEIGHT + BUSH_WEIGHT + CAVE_WEIGHT + KOULOU_WEIGHT) {
            return LovTile.Terrain.KOULOU;
        }
        return LovTile.Terrain.OBSTACLE;
    }
}
