package model.entity;

import util.GameConfig;

/**
 * Represents a Paladin hero character in the game.
 * Paladins have balanced attributes and gain strength and dexterity upon leveling up.
 ** @author Serena N
 * @version 1.0
 */
public class Paladin extends Hero {
    public Paladin(String name, double mana, double str, double agi, double dex, double money, double exp) {
        super(name, mana, str, agi, dex, money, exp);
    }

    @Override
    public void levelUp() {
        super.applyStandardLevelUp();
        this.strength *= GameConfig.SKILL_SCALE;
        this.dexterity *= GameConfig.SKILL_SCALE;
    }
}