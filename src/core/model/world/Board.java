package core.model.world;

public interface Board {
    Tile getTile(int row, int col);
    int getWidth();
    int getHeight();
}