package model.world;

public class InaccessibleTile extends Tile {
    public InaccessibleTile() {
        super("Inaccessible");
    }
    @Override
    public void enter() {
        System.out.println("You cannot enter this space.");
    }
}