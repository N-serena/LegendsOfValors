package games.legendsofvalors.model.world.generator;

import games.legendsofvalors.model.world.LovBoard;
import games.legendsofvalors.model.world.LovTile;

import java.util.List;
import java.util.Random;


public interface TerrainGenerator {
    // Generate terrain for the provided tile grid and lanes using the supplied random source.
    void generate(LovTile[][] tiles, List<LovBoard.Lane> lanes, Random random);
}
