package model.entity;

import model.item.Item;
import model.item.Weapon;
import model.item.Armor;
import util.GameConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract parent class for all playable characters.
 * Represents the "Model" in MVC: holds state but delegates complex logic to Controllers.
 * * @author Serena N.
 * @version 2.0 (Refactored for MVC)
 */
public abstract class Hero extends LivingEntity {
    // Primary Stats
    protected double mana;
    protected double strength;
    protected double dexterity;
    protected double agility;
    protected double gold;
    protected double experience;

    // Inventory State
    protected List<Item> inventory;
    protected Weapon equippedWeapon;
    protected Armor equippedArmor;

    /**
     * Initialize a new Hero with specific starting stats.
     */
    public Hero(String name, double mana, double str, double agi, double dex, double money, double exp) {
        super(name, 1); // Default start Level 1
        this.mana = mana;
        this.strength = str;
        this.agility = agi;
        this.dexterity = dex;
        this.gold = money;
        this.experience = exp;
        this.inventory = new ArrayList<>();
    }

    /**
     * Polymorphic method to apply class-specific stat bonuses.
     * Called by HeroController during the level-up process.
     */
    public abstract void levelUp();

    /**
     * Applies the base stat increases common to all Hero types.
     * Uses GameConfig constants for scalability.
     */
    protected void applyStandardLevelUp() {
        this.level++;
        this.hp = this.level * GameConfig.HP_PER_LEVEL;

        // Mercy rule: Ensure mana doesn't decay to zero
        if (this.mana < GameConfig.MANA_BASE_FLOOR) {
            this.mana = GameConfig.MANA_BASE_FLOOR;
        }
        this.mana = this.mana * GameConfig.MANA_SCALE;

        // Base skill scaling
        this.strength *= GameConfig.SKILL_SCALE;
        this.agility *= GameConfig.SKILL_SCALE;
        this.dexterity *= GameConfig.SKILL_SCALE;
    }

    // --- STATE MUTATORS ---

    public void addGold(double amount) {
        this.gold += amount;
    }

    public void decreaseGold(double amount) {
        this.gold = Math.max(0, this.gold - amount);
    }

    public void addItem(Item item) {
        inventory.add(item);
    }

    /**
     * Removes item from the list.
     * Note: Auto-unequip logic is handled by InventoryController before calling this.
     */
    public void removeItemFromList(Item item) {
        inventory.remove(item);
    }

    // --- GETTERS & SETTERS ---
    public void setEquippedWeapon(Weapon w) { this.equippedWeapon = w; }
    public void setEquippedArmor(Armor a) { this.equippedArmor = a; }

    public void setExperience(double experience) { this.experience = experience; }
    public void addExperience(double amount) { this.experience += amount; }

    public List<Item> getInventory() { return inventory; }
    public void setMana(double mana) { this.mana = mana; }
    public void setStrength(double strength) { this.strength = strength; }
    public void setDexterity(double dexterity) { this.dexterity = dexterity; }
    public void setAgility(double agility) { this.agility = agility; }
    public double getMana() { return mana; }
    public double getStrength() { return strength; }
    public double getDexterity() { return dexterity; }
    public double getAgility() { return agility; }
    public double getGold() { return gold; }
    public double getExperience() { return experience; }
    public Weapon getEquippedWeapon() { return equippedWeapon; }
    public Armor getEquippedArmor() { return equippedArmor; }

    @Override
    public String toString() {
        return name + " | HP: " + hp + " | Level: " + level;
    }
}