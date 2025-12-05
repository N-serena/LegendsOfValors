package model.entity;

import util.GameConfig;

/**
 * Abstract parent class for all playable characters.
 * Represents the "Model" in MVC: holds state but delegates complex logic to Controllers.
 * @author Serena N.
 * @version 2.0
 */
public class Warrior extends Hero {
    public Warrior(String name, double mana, double str, double agi, double dex, double money, double exp) {
        super(name, mana, str, agi, dex, money, exp);
    }

    @Override
    public void levelUp() {
        super.applyStandardLevelUp(); // Base +5%
        // Specific Feature: Warrior favors Strength and Agility
        this.strength *= GameConfig.SKILL_SCALE;
        this.agility *= GameConfig.SKILL_SCALE;
    }
}