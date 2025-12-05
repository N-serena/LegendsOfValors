package model.entity;

import util.GameConfig;

/**
 * Represents a Sorcerer hero character in the game.
 * Inherits from the Hero class and implements specific level-up behavior.
 ** @author Serena N
 * @version 1.0
 */
public class Sorcerer extends Hero {
    public Sorcerer(String name, double mana, double str, double agi, double dex, double money, double exp) {
        super(name, mana, str, agi, dex, money, exp);
    }

    @Override
    public void levelUp() {
        super.applyStandardLevelUp();
        this.dexterity *= GameConfig.SKILL_SCALE;
        this.agility *= GameConfig.SKILL_SCALE;
    }
}