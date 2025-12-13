package games.legendsofvalors.states;

import games.legendsofvalors.commands.LovCommand;
import games.legendsofvalors.commands.MoveCommand;
import games.legendsofvalors.controller.LovGameController;

import games.legendsofvalors.controller.LovGameController;
import games.legendsofvalors.commands.*;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.model.world.LovBoard;

import java.util.Map;
import java.util.Scanner;

public class HeroTurnState implements LovGameState {

    @Override
    public void execute(LovGameController context) {
        System.out.println("\n=== HERO TURN ===");
        LovBoard board = context.getBoard();
        Scanner scanner = context.getScanner();

        // 1. Iterate through all heroes in the party (Assuming context has access to party list)
        // For this example, we iterate through the heroes on the board or a list in Context
        for (ValorHero hero : context.getHeroes()) {
            if (hero.isFainted()) {
                System.out.println(hero.getName() + " is fainted and cannot act.");
                continue;
            }

            boolean turnComplete = false;
            while (!turnComplete) {
                System.out.println("\nAction for " + hero.getName() + " (" + hero.getLane() + " Lane):");
                System.out.println("[W/A/S/D] Move | [T] Teleport | [A] Attack | [R] Recall | [I] Info/Equip | [Q] Quit");
                System.out.print("> ");
                String input = scanner.next().toUpperCase();

                LovCommand command = null;

                switch (input) {
                    // Movement Commands
                    case "W": command = new MoveCommand(board, hero, -1, 0); break; // North
                    case "A": command = new MoveCommand(board, hero, 0, -1); break; // West
                    case "S": command = new MoveCommand(board, hero, 1, 0); break;  // South
                    case "D": command = new MoveCommand(board, hero, 0, 1); break;  // East
                    //Action Commands
                    case "M":
                        // check if hero is on their specific Nexus (or ANY Nexus? Rules say 'their' nexus usually)
                        LovBoard.Position currentPos = board.getHeroPosition(hero);
                        if (currentPos != null && currentPos.row == LovBoard.BOARD_SIZE - 1) { // Row 7 is Nexus
                            // Reuse the MarketController from the old game?
                            // Or simpler: Just print "Market not implemented in demo" if you don't have the controller link.
                            // Ideally: context.getMarketController().enterMarket(...)
                            System.out.println("Market entered (Simulation). Bought Potion.");
                            // Implement actual integration if you have MarketController in LovGameController
                        } else {
                            System.out.println("You must be at the Nexus to shop!");
                        }
                        break;
                    case "T":
                        // Simplified Teleport Selection for brevity
                        // In real code, ask user for target hero here
                        System.out.println("Select target hero index...");
                        // command = new TeleportCommand(board, hero, selectedTarget);
                        break;
                    case "R": command = new RecallCommand(board, hero); break;
                    case "K":
                        // Simplified Attack Selection
                        System.out.println("Attacking nearest enemy...");
                        //command = new AttackCommand(board, hero);
                        break;
                    case "Q": System.exit(0); break;
                    default: System.out.println("Invalid command.");
                }

                // Execute Command
                if (command != null) {
                    if (command.execute()) {
                        turnComplete = true; // Action successful, next hero
                    }
                }
            }

            // Check Win Condition after every move [cite: 131]
            if (board.getHeroPosition(hero).row == 0) {
                System.out.println("VICTORY! " + hero.getName() + " reached the Nexus!");
                System.exit(0);
            }
        }

        // State Transition: Heroes are done -> Monsters Turn
        context.setState(new MonsterTurnState());
    }

    // Helper to auto-target the nearest monster
    private ValorMonster findTarget(LovBoard board, ValorHero hero) {
        LovBoard.Position hPos = board.getHeroPosition(hero);
        for (int r = hPos.row - 1; r <= hPos.row + 1; r++) {
            for (int c = hPos.col - 1; c <= hPos.col + 1; c++) {
                // Skip the hero's own center tile (optional, but good practice)
                if (r == hPos.row && c == hPos.col) continue;

                // We need to find if a monster is at (r, c).
                // Since LovBoard doesn't have 'getMonsterAt', we iterate the known monsters.
                // (Optimized: You should add getMonsterAt to LovBoard, but this works for P3 isolation)
                for (core.model.entity.Monster m : core.model.GameDatabase.getInstance().getAllMonsters()) {
                    if (m instanceof ValorMonster) {
                        LovBoard.Position mPos = board.getMonsterPosition((ValorMonster) m);
                        if (mPos != null && mPos.row == r && mPos.col == c) {
                            return (ValorMonster) m;
                        }
                    }
                }
            }
        }
        return null;
    }
}