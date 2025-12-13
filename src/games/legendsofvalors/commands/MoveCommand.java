package games.legendsofvalors.commands;

import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.model.world.LovBoard;

public class MoveCommand implements LovCommand {
    private ValorHero hero;
    private int deltaRow;
    private int deltaCol;
    private LovBoard board;

    public MoveCommand(LovBoard board, ValorHero hero, int dr, int dc) {
        this.board = board;
        this.hero = hero;
        this.deltaRow = dr;
        this.deltaCol = dc;
    }

    @Override
    public boolean execute() {
        // 1. Get Current Position
        LovBoard.Position currentPos = board.getHeroPosition(hero);
        if (currentPos == null) return false;

        int targetRow = currentPos.row + deltaRow;
        int targetCol = currentPos.col + deltaCol;

        // 2. Validate "No Diagonal" Rule
        // If both changed, it's diagonal (unless one is 0).
        if (deltaRow != 0 && deltaCol != 0) {
            System.out.println("Invalid Move: Diagonal movement is forbidden.");
            return false;
        }

        // 3. Validate "Cannot Move Behind Monster" Rule
        // We only care if we are moving FORWARD (decreasing row index, e.g., 7 -> 6)
        // or sideways while behind a monster.
        ValorMonster laneMonster = board.getLeadingMonsterInLane(currentPos.col);

        if (laneMonster != null) {
            LovBoard.Position monsterPos = board.getMonsterPosition(laneMonster);
            // If the hero is currently "in front" (higher row index) of the monster
            // and tries to move to a row "behind" (lower/equal row index) the monster...
            if (currentPos.row > monsterPos.row && targetRow <= monsterPos.row) {
                System.out.println("Blocked! You cannot move behind " + laneMonster.getName() + " without killing it first!");
                return false;
            }
            // Edge Case: If in adjacent column of same lane, ensure we don't slip past
            if (currentPos.row > monsterPos.row && targetRow <= monsterPos.row && currentPos.col != monsterPos.col) {
                System.out.println("Blocked! The monster in this lane prevents passing.");
                return false;
            }
        }

        // 4. Execute Move on Board
        // The board handles wall checks and occupancy checks internally
        if (board.moveHero(hero, targetRow, targetCol)) {
            return true; // Success
        } else {
            System.out.println("Move Blocked (Wall or Occupied).");
            return false;
        }
    }
}