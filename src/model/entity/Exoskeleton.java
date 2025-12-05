package model.entity;

/**
 * Represents an Exoskeleton monster in the game.
 * Inherits from the Monster class.
 * Author: Serena N.
 * version 1.0
 */
public class Exoskeleton extends Monster {
    public Exoskeleton(String name, int level, double damage, double defense, double dodgeChance) {
        super(name, level, damage, defense, dodgeChance);
    }
}