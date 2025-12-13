package games.legendsofvalors.commands;

import games.legendsofvalors.controller.LovCombat;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.model.world.LovBoard;

public class AttackCommand implements LovCommand {
    private LovBoard board;
    private ValorHero hero;
    private ValorMonster target;
    private LovCombat combatEngine;

    public AttackCommand(LovBoard board, ValorHero hero, ValorMonster target) {
        this.board = board;
        this.hero = hero;
        this.target = target;
        this.combatEngine = new LovCombat(board); // Instantiate the math engine
    }

    @Override
    public boolean execute() {
        if (target == null) return false;

        LovBoard.Position hPos = board.getHeroPosition(hero);
        LovBoard.Position mPos = board.getMonsterPosition(target);

        // 1. Verify Range (using P2's logic)
        if (!combatEngine.isTargetInRange(hPos.row, hPos.col, mPos.row, mPos.col)) {
            System.out.println("Target out of range!");
            return false;
        }

        // 2. Calculate Damage (using P2's logic)
        double damage = combatEngine.calculateHeroDamage(hero, hPos.row, hPos.col);

        // 3. Apply Damage (Simple reduction for now, armor calc is usually on Monster side)
        // Note: Actual damage should subtract monster defense
        double actualDamage = Math.max(0, damage - (target.getDefense() * 0.05)); // Simplified armor math
        target.takeDamage(actualDamage);

        System.out.println(hero.getName() + " attacks " + target.getName() + " for " + (int)actualDamage + " damage!");

        // 4. Handle Death
        if (target.isFainted()) {
            System.out.println(target.getName() + " was defeated!");
            // Remove from board
            // board.removeMonster(target); // Need to ask P1 for this method!
        }

        return true;
    }
}