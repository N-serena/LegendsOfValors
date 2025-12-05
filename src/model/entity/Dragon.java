package model.entity;

/** Dragon is a type of Monster with potentially unique attributes.
 * Dragons may have higher base damage compared to other monsters.
 * Inherits from the Monster class.
 * Author: Serena N.
 * version 1.0
*/
public class Dragon extends Monster {
    public Dragon(String name, int level, double damage, double defense, double dodgeChance) {
        super(name, level, damage, defense, dodgeChance);
        // Dragons might have slightly higher base damage naturally from the file
    }
}