package model.item;

/**
 * Represents a potion item that can increase certain attributes of a character.
 ** @author Serena N
 * @version 1.0
 */
public class Potion implements Item {
    private String name;
    private double price;
    private int minLevel;
    private double attributeIncrease;
    private String statAffected; // e.g., "Health" or "Health/Mana/Strength"

    public Potion(String name, double price, int minLevel, double attributeIncrease, String statAffected) {
        this.name = name;
        this.price = price;
        this.minLevel = minLevel;
        this.attributeIncrease = attributeIncrease;
        this.statAffected = statAffected;
    }

    @Override public String getName() { return name; }
    @Override public double getPrice() { return price; }
    @Override public int getMinLevel() { return minLevel; }

    public double getAttributeIncrease() { return attributeIncrease; }
    public String getStatAffected() { return statAffected; }
}