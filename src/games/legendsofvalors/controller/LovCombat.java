package games.legendsofvalors.controller;

import core.interfaces.Board;
import core.model.entity.Hero;
import core.model.entity.Monster;
import core.model.world.Tile;

public class LovCombat {
    private Board board;

    public LovCombat(Board board) {
        this.board = board;
    }

    // --- 1. RANGE CHECKING (The 3x3 Grid) ---
    /**
     * Checks if the target is in the current space or any neighbor (diagonal included).
     * Source: "The attack range is limited to the current space and its neighbors"
     */
      public boolean isTargetInRange(int heroRow, int heroCol, int targetRow, int targetCol) {
        int rowDiff = Math.abs(heroRow - targetRow);
        int colDiff = Math.abs(heroCol - targetCol);

        // If both row and col difference is <= 1, they are adjacent or on same tile.
        return rowDiff <= 1 && colDiff <= 1;
    }

    // --- 2. DAMAGE CALCULATION (With Terrain Bonuses) ---
    /**
     * Calculates damage incorporating Terrain Bonuses.
     * Koulou Space: Increases Strength by 10% [cite: 56, 120]
     */
    public double calculateHeroDamage(Hero hero, int row, int col) {
        double strength = hero.getStrength();

        // Check Terrain for Strength Bonus
        Tile tile = board.getTile(row, col);
        if (tile.getType().equalsIgnoreCase("Koulou")) {
            strength *= 1.10; // +10% Bonus
            System.out.println("Terrain Bonus: " + hero.getName() + " gains strength from Koulou!");
        }

        // Standard MH Formula: (Str + Weapon) * 0.05 [cite: 13, 88]
        // CORRECT: Calling .getEquippedWeapon() directly on the Hero object
        double weaponDamage = (hero.getEquippedWeapon() != null)
                ? hero.getEquippedWeapon().getDamage() : 0;
        return (strength + weaponDamage) * 0.05;
    }

    // --- 3. DODGE CALCULATION (With Terrain Bonuses) ---
    /**
     * Calculates dodge chance incorporating Terrain Bonuses.
     * Cave Space: Increases Agility by 10% (Hero) or Dodge (Monster) [cite: 53, 55, 120]
     */
    public double calculateHeroDodge(Hero hero, int row, int col) {
        double agility = hero.getAgility();

        Tile tile = board.getTile(row, col);
        if (tile.getType().equalsIgnoreCase("Cave")) {
            agility *= 1.10; // +10% Bonus
            System.out.println("Terrain Bonus: " + hero.getName() + " feels agile in the Cave!");
        }

        return agility * 0.002; // Standard MH formula
    }

    // --- 4. MONSTER SPECIFIC BONUSES ---
    /**
     * Monsters also receive bonuses in specific tiles[cite: 52, 55, 58].
     */
    public void applyMonsterTerrainBuffs(Monster monster, int row, int col) {
        Tile tile = board.getTile(row, col);
        String type = tile.getType();

        if (type.equalsIgnoreCase("Koulou")) {
            // Monsters receive an attack bonus [cite: 58]
            double newDmg = monster.getBaseDamage() * 1.10;
            monster.setBaseDamage((int)newDmg);
        }
        else if (type.equalsIgnoreCase("Cave")) {
            // Monsters receive a dodge bonus [cite: 55]
            double newDodge = monster.getDodgeChance() + 0.10;
            monster.setDodgeChance(newDodge);
        }
    }
}