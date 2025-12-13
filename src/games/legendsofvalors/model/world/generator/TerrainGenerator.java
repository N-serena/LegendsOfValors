package games.legendsofvalors.model.world.generator;

import games.legendsofvalors.model.world.LovBoard;
import games.legendsofvalors.model.world.LovTile;

import java.util.List;
import java.util.Random;

/**
 * Strategy interface for populating Legends of Valor terrain tiles.
 * Legends of Valor 地形生成策略接口。
 */
public interface TerrainGenerator {
    // Generate terrain for the provided tile grid and lanes using the supplied random source.
    // 使用指定的随机源与线路信息，为给定的地块网格生成地形。
    void generate(LovTile[][] tiles, List<LovBoard.Lane> lanes, Random random);
}
