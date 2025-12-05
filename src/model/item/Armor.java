package model.item;

/**
 * Represents an armor item in the game.
 ** @author Serena N
 * @version 1.0
 */
public class Armor implements Item {
    private String name;
    private double price;
    private int minLevel;
    private double damageReduction;

    public Armor(String name, double price, int minLevel, double damageReduction) {
        this.name = name;
        this.price = price;
        this.minLevel = minLevel;
        this.damageReduction = damageReduction;
    }

    @Override public String getName() { return name; }
    @Override public double getPrice() { return price; }
    @Override public int getMinLevel() { return minLevel; }

    public double getDamageReduction() { return damageReduction; }
}