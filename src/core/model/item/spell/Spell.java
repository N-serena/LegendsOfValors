package core.model.item.spell;

import core.model.entity.Monster;
import core.model.item.Item;

/**
 * Represents a magical spell item with specific attributes.
 ** @author Serena N
 * @version 1.0
 */
public abstract class Spell implements Item {
    public enum SpellType { ICE, FIRE, LIGHTNING }

    private String name;
    private double price;
    private int minLevel;
    private double damage;
    private double manaCost;
    private SpellType type;

    public Spell(){}

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

    public abstract double getEffect();

    public void castEffect(Monster target)
    {
        target.setDefense(target.getDefense() * getEffect());
    }
}