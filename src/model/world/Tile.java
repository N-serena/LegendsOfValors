package model.world;

public abstract class Tile {
    protected String type;

    public Tile(String type) {
        this.type = type;
    }

    public String getType() { return type; }

    // Abstract method to handle what happens when player enters
    public abstract void enter();
}