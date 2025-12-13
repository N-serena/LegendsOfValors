package games.legendsofvalors.commands;

import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.world.LovBoard;

public class RecallCommand implements LovCommand {
    private LovBoard board;
    private ValorHero hero;

    public RecallCommand(LovBoard board, ValorHero hero) {
        this.board = board;
        this.hero = hero;
    }

    @Override
    public boolean execute() {
        // 1. Check if Recall is possible
        // (Optional: You might want to block recall if the nexus is occupied by another hero,
        // though the rules imply it's always safe).
        int targetRow = hero.getNexusRow();
        int targetCol = hero.getNexusCol();

        // 2. Check if the Nexus is blocked by a MONSTER (Game Over condition anyway)
        // or another HERO (Stacking usually not allowed)
        if (board.isCellBlocked(targetRow, targetCol)) {
            System.out.println("Recall Failed: Your Nexus is currently blocked!");
            return false;
        }

        // 3. Reset Hero State (HP/Mana)
        // This method is inside ValorHero (Person 2's work)
        hero.recall();

        // 4. Move Visually on Board
        // We use 'placeHero' or 'moveHero' to update the grid
        board.moveHero(hero, targetRow, targetCol);

        System.out.println(hero.getName() + " recalled to the Nexus.");
        return true;
    }
}