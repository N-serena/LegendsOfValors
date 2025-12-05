package model.item;

/**
 * Represents a weapon item in the game.
 ** @author Serena N
 * @version 1.0
 */
public class Weapon implements Item {
    private String name;
    private double price;
    private int minLevel;
    private double damage;
    private int requiredHands;

    public Weapon(String name, double price, int minLevel, double damage, int requiredHands) {
        this.name = name;
        this.price = price;
        this.minLevel = minLevel;
        this.damage = damage;
        this.requiredHands = requiredHands;
    }

    @Override public String getName() { return name; }
    @Override public double getPrice() { return price; }
    @Override public int getMinLevel() { return minLevel; }

    public double getDamage() { return damage; }
    public int getRequiredHands() { return requiredHands; }
}