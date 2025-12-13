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
        // Person 2's LovCombat has 'isTargetInRange', but we can also check the board directly
        // for neighbors.
        // Simplified: Check the 8 neighbors for a monster.
        LovBoard.Position hPos = board.getHeroPosition(hero);
        for (int r = hPos.row - 1; r <= hPos.row + 1; r++) {
            for (int c = hPos.col - 1; c <= hPos.col + 1; c++) {
                if (board.getTile(r, c) != null) { // Check bounds implicitly via getTile safely?
                    // Better: ask board for monster at (r,c)
                    // We need a method in LovBoard: getMonsterAt(r, c)
                    // Assuming we can access the monster map or iterate:
                    // For now, let's iterate board.getMonstersReadyToAttack(1) or similar logic
                }
            }
        }
        // Since we don't have a clean "getMonsterAt" helper yet, let's ask Person 1 for it later.
        // For now, we return null to allow compilation.
        return null;
    }

    //getMonsterAt(r,c): Ask Person 1 (Map) to add a helper so you can easily find the target for the attack command.
}