package model.item;

/**
 * Represents a magical spell item with specific attributes.
 ** @author Serena N
 * @version 1.0
 */
public class Spell implements Item {
    public enum SpellType { ICE, FIRE, LIGHTNING }

    private String name;
    private double price;
    private int minLevel;
    private double damage;
    private double manaCost;
    private SpellType type;

    public Spell(String name, double price, int minLevel, double damage, double manaCost, SpellType type) {
        this.name = name;
        this.price = price;
        this.minLevel = minLevel;
        this.damage = damage;
        this.manaCost = manaCost;
        this.type = type;
    }

    @Override public String getName() { return name; }
    @Override public double getPrice() { return price; }
    @Override public int getMinLevel() { return minLevel; }

    public double getDamage() { return damage; }
    public double getManaCost() { return manaCost; }
    public SpellType getType() { return type; }
}