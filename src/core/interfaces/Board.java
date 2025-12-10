package core.interfaces;

import core.model.world.Tile;

public interface Board {
    int getWidth();
    int getHeight();
    Tile getTile(int r, int c);
}