package model.entity;

import util.GameConfig;

/** Abstract class for all monsters in the game
 ** @author Serena N
 *  * @version 1.0
 */
public abstract class Monster extends LivingEntity {
    protected double baseDamage;
    protected double defense;
    protected double dodgeChance;

    public Monster(String name, int level, double damage, double defense, double dodgeChance) {
        super(name, level);
        this.baseDamage = damage;
        this.defense = defense;
        this.dodgeChance = dodgeChance;
    }

    // --- Getters and Setters ---
    public double getBaseDamage() { return baseDamage; }
    public double getDefense() { return defense; }
    public double getDodgeChance() { return dodgeChance; }
    public double setDodgeChance(double dodgeChance) {
        this.dodgeChance = dodgeChance;
        return this.dodgeChance;
    }
    public void setDefense(double defense) {
        this.defense = defense;
    }
    public void setBaseDamage(double baseDamage) {
        this.baseDamage = baseDamage;
    }

    /** When the hero levels up, new monsters need to be stronger
      * You can call this when creating a copy of a monster for battle
      */
    public void scaleStats(int level) {
        // 1. Calculate how strong the monster is PER LEVEL based on the file data
        // (e.g., Alexstraszan is 9000 def at lvl 10 -> 900 def per level)
        double damagePerLevel = this.baseDamage / this.level;
        double defensePerLevel = this.defense / this.level;

        // 2. Apply that ratio to the NEW level
        this.baseDamage = damagePerLevel * level;
        this.defense = defensePerLevel * level;

        // 3. Set new HP and Level
        this.hp = level * GameConfig.HERO_HP_SCALING;
        this.level = level;
    }

    @Override
    public String toString() {
        return name + " (Lvl " + level + ") - HP: " + hp;
    }
}