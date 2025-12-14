package games.legendsofvalors.commands;

import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.world.LovBoard;

public class TeleportCommand implements LovCommand {
    private LovBoard board;
    private ValorHero hero;
    private int destRow;
    private int destCol;

    /**
     * Constructor accepts the FINAL destination.
     * The validation logic (checking lanes, neighbors, blocking)
     * is handled by the Controller/State before this object is created.
     */
    public TeleportCommand(LovBoard board, ValorHero hero, int r, int c) {
        this.board = board;
        this.hero = hero;
        this.destRow = r;
        this.destCol = c;
    }

    @Override
    public boolean execute() {
        System.out.println("Teleporting " + hero.getName() + " to (" + destRow + ", " + destCol + ")...");
        return board.moveHero(hero, destRow, destCol);
    }
}