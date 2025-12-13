package games.legendsofvalors.commands;

import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.world.LovBoard;

public class TeleportCommand implements LovCommand {
    private ValorHero hero;
    private ValorHero targetHero; // The hero we are teleporting TO
    private LovBoard board;

    public TeleportCommand(LovBoard board, ValorHero hero, ValorHero targetHero) {
        this.board = board;
        this.hero = hero;
        this.targetHero = targetHero;
    }

    @Override
    public boolean execute() {
        LovBoard.Position currentPos = board.getHeroPosition(hero);
        LovBoard.Position targetPos = board.getHeroPosition(targetHero);

        // Rule 1: Must be in a different lane [cite: 120]
        // (We can assume column difference > 1 implies different lane or check specific lane logic)
        if (Math.abs(currentPos.col - targetPos.col) < 2) {
            System.out.println("Teleport Failed: Cannot teleport to the same lane.");
            return false;
        }

        // Rule 2: Cannot land "ahead" of the target (closer to enemy nexus/Row 0) [cite: 121]
        // We must land adjacent (row, col-1) or (row, col+1) or (row-1/row+1)?
        // The diagram implies teleporting to the SIDE of the target.
        // We will try to place the hero adjacent to the target.

        int destRow = targetPos.row;
        int destCol = targetPos.col;

        // Try Left Side
        if (!board.isCellBlocked(destRow, destCol - 1)) {
            destCol = destCol - 1;
        }
        // Try Right Side
        else if (!board.isCellBlocked(destRow, destCol + 1)) {
            destCol = destCol + 1;
        }
        // Try Behind (Higher Row)
        else if (!board.isCellBlocked(destRow + 1, destCol)) {
            destRow = destRow + 1;
        }
        else {
            System.out.println("Teleport Failed: No valid space adjacent to " + targetHero.getName());
            return false;
        }

        // Execute
        System.out.println("Teleporting to " + targetHero.getName() + "...");
        return board.moveHero(hero, destRow, destCol);
    }
}