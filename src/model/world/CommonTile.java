package model.world;

public class CommonTile extends Tile {
    public CommonTile() {
        super("Common");
    }
    @Override
    public void enter() {
        // Logic handled by controller (Dice roll for battle)
    }
}