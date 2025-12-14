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

        for (ValorHero hero : context.getHeroes()) {
            if (hero.isFainted()) {
                System.out.println(hero.getName() + " is fainted and cannot act.");
                continue;
            }

            boolean turnComplete = false;
            while (!turnComplete) {
                System.out.println(board.renderColored());
                System.out.println("\nAction for " + hero.getName() + " (" + hero.getLane() + " Lane):");
                System.out.println("[W/A/S/D] Move | [T] Teleport | [K] Attack | [C] Cast Spell | [R] Recall");
                System.out.println("[M] Market | [I] Info | [E] Equip/Item | [Q] Quit");
                System.out.print("> ");
                String input = scanner.next().toUpperCase();

                LovCommand command = null;

                switch (input) {
                    // Movement
                    case "W": command = new MoveCommand(board, hero, -1, 0); break;
                    case "A": command = new MoveCommand(board, hero, 0, -1); break;
                    case "S": command = new MoveCommand(board, hero, 1, 0); break;
                    case "D": command = new MoveCommand(board, hero, 0, 1); break;

                    // Actions
                    case "T": command = handleTeleportInput(scanner, board, hero, context.getHeroes()); break;
                    case "R": command = new RecallCommand(board, hero); break;
                    case "K": command = handleAttackInput(board, hero, context); break;
                    case "C": command = handleSpellInput(board, hero, context); break;
                    case "M": handleMarketInput(scanner, board, hero, context); break;

                    // SEPARATED INFO & EQUIP
                    case "I": handleInfoInput(hero); break; // Just Stats
                    case "E": handleEquipInput(hero, context); break; // Equip & Potions

                    case "Q": System.exit(0); break;
                    default: System.out.println("Invalid command.");
                }

                if (command != null) {
                    if (command.execute()) {
                        turnComplete = true;
                    }
                }
            }

            // Win Condition
            if (board.getHeroPosition(hero).row == 0) {
                System.out.println("VICTORY! " + hero.getName() + " reached the Nexus!");
                System.exit(0);
            }
        }
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

    // --- Handlers ---
    private LovCommand handleTeleportInput(Scanner scanner, LovBoard board, ValorHero currentHero, List<ValorHero> party) {
        System.out.println("Select a hero to teleport to:");

        // 1. SELECT TARGET HERO
        List<ValorHero> validTargets = new java.util.ArrayList<>();
        for (ValorHero h : party) {
            if (h != currentHero) {
                System.out.println((validTargets.size() + 1) + ". " + h.getName() + " (Lane: " + h.getLane() + ")");
                validTargets.add(h);
            }
        }

        if (validTargets.isEmpty()) {
            System.out.println("No targets available.");
            return null;
        }

        System.out.print("Enter Target ID (0 to cancel): ");
        if (!scanner.hasNextInt()) { scanner.next(); return null; }
        int targetIdx = scanner.nextInt();
        if (targetIdx <= 0 || targetIdx > validTargets.size()) return null;

        ValorHero targetHero = validTargets.get(targetIdx - 1);
        LovBoard.Position tPos = board.getHeroPosition(targetHero);
        LovBoard.Position cPos = board.getHeroPosition(currentHero);

        // Rule: Different Lane Check
        if (Math.abs(cPos.col - tPos.col) <= 1) { // Simple col distance check for 'same lane' approximation or use strict lane check
            // Better: Check if they share the same Lane object
            if (board.getLaneForColumn(cPos.col) == board.getLaneForColumn(tPos.col)) {
                System.out.println("Cannot teleport to the same lane.");
                return null;
            }
        }

        // 2. GENERATE CANDIDATES (Adjacent: Left, Right, Behind)
        // "Cannot teleport to a space ahead" -> Row - 1 is forbidden.
        List<LovBoard.Position> candidates = new java.util.ArrayList<>();
        candidates.add(new LovBoard.Position(tPos.row, tPos.col - 1)); // Left
        candidates.add(new LovBoard.Position(tPos.row, tPos.col + 1)); // Right
        candidates.add(new LovBoard.Position(tPos.row + 1, tPos.col)); // Behind

        // 3. FILTER CANDIDATES
        List<LovBoard.Position> validMoves = new java.util.ArrayList<>();

        for (LovBoard.Position p : candidates) {
            // Check Bounds & Blocking (Hero/Wall)
            if (board.isCellBlocked(p.row, p.col)) continue;

            // Rule: "Cannot teleport behind a monster"
            // We check if the destination row is "behind" (<=) the leading monster in that column
            games.legendsofvalors.model.ValorMonster leader = board.getLeadingMonsterInLane(p.col);
            if (leader != null) {
                int monsterRow = board.getMonsterPosition(leader).row;
                if (p.row <= monsterRow) {
                    // Blocked by monster line
                    continue;
                }
            }
            validMoves.add(p);
        }

        // 4. USER PICK (If multiple)
        if (validMoves.isEmpty()) {
            System.out.println("Teleport Failed: No valid open spots around " + targetHero.getName());
            return null;
        }

        LovBoard.Position finalDest;
        if (validMoves.size() == 1) {
            finalDest = validMoves.get(0);
        } else {
            System.out.println("Select destination:");
            for (int i = 0; i < validMoves.size(); i++) {
                LovBoard.Position p = validMoves.get(i);
                String desc = (p.row > tPos.row) ? "Behind" : (p.col < tPos.col ? "Left" : "Right");
                System.out.println((i + 1) + ". " + desc + " (" + p.row + ", " + p.col + ")");
            }
            System.out.print("> ");
            if (!scanner.hasNextInt()) { scanner.next(); return null; }
            int moveIdx = scanner.nextInt();
            if (moveIdx < 1 || moveIdx > validMoves.size()) return null;
            finalDest = validMoves.get(moveIdx - 1);
        }
        return new TeleportCommand(board, currentHero, finalDest.row, finalDest.col);
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
    private void handleEquipInput(ValorHero hero, LovGameController context) {
        Scanner scanner = context.getScanner();
        System.out.println("\n--- INVENTORY ACTION ---");
        System.out.println("1. Equip Weapon/Armor");
        System.out.println("2. Use Potion");
        System.out.println("0. Back");
        System.out.print("> ");

        if (scanner.hasNextInt()) {
            int choice = scanner.nextInt();
            if (choice == 1) {
                // Delegate to P2's Inventory Controller
                context.getInventoryController().openEquipMenu(hero);
            } else if (choice == 2) {
                context.getInventoryController().openPotionMenu(hero);
            }
        } else {
            scanner.next(); // Clear invalid input
        }
    }
    private void handleInfoInput(ValorHero hero) {
        System.out.println("\nStats for " + hero.getName());
        System.out.println("HP: " + hero.getHp() + " | Mana: " + hero.getMana());
        System.out.println("Str: " + hero.getStrength() + " | Dex: " + hero.getDexterity() + " | Agi: " + hero.getAgility());
        System.out.println("Gold: " + hero.getGold() + " | XP: " + hero.getExperience());

        // Handle List<Weapon> instead of single Weapon
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