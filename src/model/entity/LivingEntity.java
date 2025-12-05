package model.entity;

/**
 * Abstract class representing a living entity in the game.
 * This can be a player character, NPC, or monster.
 ** @author Serena N
 * @version 1.0
 */
public abstract class LivingEntity {
    protected String name;
    protected int level;
    protected double hp;

    public LivingEntity(String name, int level) {
        this.name = name;
        this.level = level;
        // HP Formula: Level * 100
        this.hp = level * 100;
    }

    public String getName() { return name; }
    public int getLevel() { return level; }
    public double getHp() { return hp; }

    public void setHp(double hp) {
        this.hp = hp;
    }

    public boolean isFainted() {
        return hp <= 0;
    }

    // Common method to take damage
    public void takeDamage(double amount) {
        this.hp -= amount;
        if (this.hp < 0) this.hp = 0;
    }
}