package model.world;

import model.item.Item;
import util.GameConfig;

import java.util.List;
import java.util.Random;

public class Board {
    private int width;
    private int height;
    private Tile[][] grid;
    private List<Item> possibleItems;

    public Board(int width, int height, List<Item> items) {
        this.width = width;
        this.height = height;
        this.possibleItems = items;
        this.grid = new Tile[height][width];
        initializeBoard();
    }

    private void initializeBoard() {
        Random rand = new Random();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // ------------------------
                // Ensure (0,0) and its immediate neighbors (0,1) and (1,0) are never walls.
                // This prevents the player from being trapped instantly.
                if ((i == 0 && j == 0) || (i == 0 && j == 1) || (i == 1 && j == 0)) {
                    // Force them to be Common or Market, never Inaccessible
                    if (rand.nextBoolean()) {
                        grid[i][j] = new CommonTile();
                    } else {
                        // Pass items if it happens to be a market (optional, or just make them common)
                        grid[i][j] = new CommonTile();
                    }
                    continue; // Skip the standard randomizer for these tiles
                }
                // ------------------------

                int roll = rand.nextInt(10); // 0-9

                // 20% Inaccessible (0,1)
                if (roll < GameConfig.CHANCE_WALL * 10) {
                    grid[i][j] = new InaccessibleTile();
                }
                // 30% Market (2,3,4)
                else if (roll < GameConfig.CHANCE_WALL * 10 + GameConfig.CHANCE_MARKET * 10) {
                    grid[i][j] = new MarketTile(possibleItems);
                }
                // 50% Common (5,6,7,8,9)
                else {
                    grid[i][j] = new CommonTile();
                }
            }
        }
    }

    public Tile getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return null;
        return grid[y][x];
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}