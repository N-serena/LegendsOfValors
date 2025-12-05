package controller;

import model.entity.Hero;
import util.GameConfig;
import view.Colors;

/**
 * Manages the business logic for Hero entities.
 * Handles Experience loops, Level Up events, and Combat Math calculations.
 * * @author Serena N.
 * @version 2.0 (Refactored)
 */
public class HeroController {

    /**
     * Processes experience gain.
     * Checks if the hero has leveled up (potentially multiple times)
     * and triggers the state update.
     */
    public void gainExperience(Hero hero, double amount) {
        hero.addExperience(amount);

        // XP Loop: Level * 10 required per level
        while (hero.getExperience() >= hero.getLevel() * 10) {
            double cost = hero.getLevel() * 10;
            hero.setExperience(hero.getExperience() - cost);

            // Trigger the polymorphic state update
            hero.levelUp();

            // Notify (Side effect allowed here for feedback)
            System.out.println(Colors.GREEN + hero.getName() + " LEVELED UP TO " + hero.getLevel() + "!" + Colors.RESET);
        }
    }

    /**
     * Calculates attack damage based on Hero Stats + Gear.
     * Formula: (Strength + WeaponDmg) * 0.05
     */
    public double calculateDamage(Hero hero) {
        double weaponDamage = (hero.getEquippedWeapon() != null) ? hero.getEquippedWeapon().getDamage() : 0;
        return (hero.getStrength() + weaponDamage) * GameConfig.DAMAGE_SCALE;
    }

    /**
     * Calculates dodge chance based on Agility.
     * Formula: Agility * 0.002 (Capped at 50%)
     */
    public double calculateDodgeChance(Hero hero) {
        double chance = hero.getAgility() * GameConfig.DODGE_SCALE;
        if (chance > GameConfig.MAX_DODGE_CHANCE) return GameConfig.MAX_DODGE_CHANCE;
        return chance;
    }

    /**
     * Calculates spell damage based on Hero Dexterity + Spell Base Dmg.
     * Formula: Base + (Dex/10000 * Base)
     */
    public double calculateSpellDamage(model.entity.Hero hero, model.item.Spell spell) {
        return spell.getDamage() + (hero.getDexterity() / 10000.0) * spell.getDamage();
    }
}