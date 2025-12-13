package games.legendsofvalors.states;

import games.legendsofvalors.commands.LovCommand;
import games.legendsofvalors.commands.MoveCommand;
import games.legendsofvalors.controller.LovGameController;

import games.legendsofvalors.commands.*;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.model.world.LovBoard;

import java.util.List;
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
                System.out.println(board.renderColored());
                System.out.println("\nAction for " + hero.getName() + " (" + hero.getLane() + " Lane):");
                System.out.println("[W/A/S/D] Move | [T] Teleport | [K] Attack | [C] Cast Spell | [R] Recall | [M] Market | [I] Info/Equip | [Q] Quit");
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
                    case "M": handleMarketInput(scanner, board, hero, context); break;
                    case "T": command = handleTeleportInput(scanner, board, hero, context.getHeroes()); break;
                    case "R": command = new RecallCommand(board, hero); break;
                    case "K": command = handleAttackInput(board, hero, context); break;
                    case "C": command = handleSpellInput(board, hero, context); break;
                    case "I": handleInfoInput(hero); break;
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

    // --- Refactored Handlers  ---
    private LovCommand handleTeleportInput(Scanner scanner, LovBoard board, ValorHero currentHero, List<ValorHero> party) {
        System.out.println("Select a hero to teleport to:");

        // 1. List available targets
        boolean hasTargets = false;
        for (int i = 0; i < party.size(); i++) {
            ValorHero h = party.get(i);
            // Don't list yourself
            if (h != currentHero) {
                System.out.println((i + 1) + ". " + h.getName() + " (Lane: " + h.getLane() + ")");
                hasTargets = true;
            }
        }

        if (!hasTargets) {
            System.out.println("No valid targets available.");
            return null;
        }

        System.out.print("Enter Hero ID (0 to cancel): ");

        // 2. Get User Input & Validate
        if (scanner.hasNextInt()) {
            int targetIdx = scanner.nextInt();

            // Check Bounds
            if (targetIdx > 0 && targetIdx <= party.size()) {
                ValorHero targetHero = party.get(targetIdx - 1);

                // 3. Create the Command
                if (targetHero == currentHero) {
                    System.out.println("You cannot teleport to yourself.");
                    return null;
                }

                // Return the configured command for the main loop to execute
                return new TeleportCommand(board, currentHero, targetHero);

            } else if (targetIdx != 0) {
                System.out.println("Invalid Hero ID.");
            }
        } else {
            scanner.next(); // Clear invalid input buffer
            System.out.println("Invalid input.");
        }

        return null; // Return null if cancelled or invalid
    }
    private LovCommand handleAttackInput(LovBoard board, ValorHero hero, LovGameController context) {
        ValorMonster target = findTarget(board, hero);
        if (target != null) {
            return new AttackCommand(board, hero, target, context.getParty());
        } else {
            System.out.println("No monsters in range (Range: 1).");
            return null;
        }
    }
    private LovCommand handleSpellInput(LovBoard board, ValorHero hero, LovGameController context) {
        // Reuse your existing targeting logic
        ValorMonster target = findTarget(board, hero);

        if (target != null) {
            // Return the new Spell Command
            return new CastSpellCommand(board, hero, target, context.getParty());
        } else {
            System.out.println("No monsters in range to cast spells on.");
            return null;
        }
    }
    private void handleMarketInput(Scanner scanner, LovBoard board, ValorHero hero, LovGameController context) {
        LovBoard.Position currentPos = board.getHeroPosition(hero);

        // 1. Check Rule: Must be on Hero Nexus (Row 7)
        // LovTile.java defines nexus types, but checking row index is the safest quick check for now.
        if (currentPos != null && currentPos.row == games.legendsofvalors.util.GameConfig.BOARD_SIZE - 1) {
            System.out.println("Entering the Nexus Market...");

            // 2. Reuse MarketController from the Context
            // This avoids null pointer errors or creating duplicate scanners
            games.commoncontrollers.MarketController marketCtrl = context.getMarketController();

            // 3. Create a fresh Market (Nexus has all items)
            // Using Singleton Data from GameDatabase
            core.model.market.Market nexusMarket = new core.model.market.Market(
                    core.model.GameDatabase.getInstance().getAllItems()
            );

            // 4. Launch Market Logic
            // handleShopper takes (Hero, Market)
            marketCtrl.handleShopper(hero, nexusMarket);

            System.out.println("Exited Market.");

        } else {
            System.out.println("You must be at the Nexus to shop!");
        }
    }
    private void handleInfoInput(ValorHero hero) {
        System.out.println("\nStats for " + hero.getName());
        System.out.println("HP: " + hero.getHp() + " | Mana: " + hero.getMana());
        System.out.println("Str: " + hero.getStrength() + " | Dex: " + hero.getDexterity() + " | Agi: " + hero.getAgility());
        System.out.println("Gold: " + hero.getGold() + " | XP: " + hero.getExperience());

        // FIX: Handle List<Weapon> instead of single Weapon
        System.out.print("Equipped Weapons: ");
        java.util.List<core.model.item.Weapon> weapons = hero.getEquippedWeapon();

        if (weapons == null || weapons.isEmpty()) {
            System.out.println("None");
        } else {
            // Print all equipped weapons
            for (core.model.item.Weapon w : weapons) {
                System.out.print(w.getName() + " ");
            }
            System.out.println(); // New line
        }

        // Armor is still a single item
        System.out.println("Equipped Armor: " + (hero.getEquippedArmor() != null ? hero.getEquippedArmor().getName() : "None"));
    }
}