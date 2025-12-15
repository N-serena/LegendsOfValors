package games.legendsofvalors.states;

import core.model.item.Item;
import core.model.item.Potion;
import core.model.item.spell.Spell;
import games.commoncontrollers.InputHandler;
import games.legendsofvalors.interfaces.LovCommand;
import games.legendsofvalors.commands.MoveCommand;
import games.legendsofvalors.controller.LovGameController;

import games.legendsofvalors.commands.*;
import games.legendsofvalors.interfaces.LovGameState;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.model.world.LovBoard;
import games.monstersandheroes.view.Colors;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Math.ceil;

public class HeroTurnState implements LovGameState {

    InputHandler inputHandler;

    public HeroTurnState() {
        inputHandler = new InputHandler();
    }

    @Override
    public void execute(LovGameController context) {
        printRoundBanner(context);
        displayStatus(context);
        LovBoard board = context.getBoard();
        Scanner scanner = context.getScanner();
        int i = 1;

        context.displayBoard();

        for (ValorHero hero : context.getHeroes()) {
            if (hero.isFainted()) {
                System.out.println(hero.getName() + " is fainted and cannot act.");
                continue;
            }
            boolean turnComplete = false;
            while (!turnComplete) {
                System.out.println("\n[H" + i + "] Action for " + hero.getName() + " (" + hero.getLane() + " Lane):");
                System.out.println();
                System.out.println("[W/A/S/D] Move | [T] Teleport | [K] Attack | [C] Cast Spell | [R] Recall");
                System.out.println("[M] Market | [I] Info | [E] Equip/Item | [L] Legend | [Q] Quit");
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
                    case "L": 
                        System.out.println("\n" + board.getLegendText());
                        System.out.println("\nPress Enter to continue...");
                        scanner.nextLine(); // Consume newline
                        scanner.nextLine(); // Wait for user
                        break;

                    case "Q": context.isRunning = false; turnComplete = true; break;
                    default: System.out.println("Invalid command.");
                }


                if (command != null) {
                    if (command.execute()) {
                        context.displayBoard();
                        i++;
                        turnComplete = true;
                    }
                }
            }

            // Win Condition
            if (board.getHeroPosition(hero).row == 0) {
                System.out.println("VICTORY! " + hero.getName() + " reached the Nexus!");
                context.isRunning = false;
                context.displayEndGameStats("won");
                //System.exit(0);
            }
            if (!context.isRunning)
            {
                break;
            }
        }

//        inputHandler.enter();
          context.setState(new MonsterTurnState());
    }

    // Helpers
    private ValorMonster findTarget(LovBoard board, ValorHero hero) {
        LovBoard.Position hPos = board.getHeroPosition(hero);

        //List<ValorMonster> potentialTargets = new ArrayList<>(board.getMonstersInRange(hero, 1));
        List<ValorMonster> potentialTargets = new ArrayList<>();


        // Iterate through ACTIVE monsters on the board, not the database templates
        for (java.util.Map.Entry<ValorMonster, LovBoard.Position> entry : board.getMonsterPositions().entrySet()) {
            LovBoard.Position mPos = entry.getValue();

            // Check for adjacency (3x3 grid around hero)
            // Logic: Row difference <= 1 AND Col difference <= 1
            if (Math.abs(hPos.row - mPos.row) <= 1 && Math.abs(hPos.col - mPos.col) <= 1) {
                //return entry.getKey(); // Found a target!
                potentialTargets.add(entry.getKey());
            }
        }

        if (potentialTargets.size() == 0)
        {
            return null;
        }

        if (potentialTargets.size() == 1)
        {
            //only one monster in range
            return potentialTargets.get(0);
        }
        else {
            //else give the choice to hero to select which monster to attack
            int i = 1;
            int choice;

            for (ValorMonster monster : potentialTargets) {
                System.out.println("[" + i + "] " + monster);
                i++;
            }
            System.out.println();
            System.out.println("Choose your target !");

            choice = inputHandler.getIntegerInput(1, potentialTargets.size());

            return potentialTargets.get(choice - 1);
        }
    }
    private void printRoundBanner(LovGameController context) {
        int currentRound = context.getRoundNumber();
        int interval = games.legendsofvalors.util.GameConfig.DEFAULT_SPAWN_INTERVAL;

        // Wave happens when (round % 8 == 0) at the END of the round.
        // If round is 8, remainder is 0 -> Wave is imminent.
        int remainder = currentRound % interval;
        int roundsLeft = (remainder == 0) ? 0 : (interval - remainder);

        System.out.println("\n" + games.monstersandheroes.view.Colors.YELLOW + "========================================");
        System.out.println("             ROUND " + currentRound);

        if (remainder == 0) {
            System.out.println(games.monstersandheroes.view.Colors.RED + "   ⚠️  MONSTER WAVE SPAWNS THIS TURN!  ⚠️" + games.monstersandheroes.view.Colors.YELLOW);
        } else {
            System.out.println("   Next Monster Wave in: " + roundsLeft + " rounds");
        }
        System.out.println("========================================" + games.monstersandheroes.view.Colors.RESET);
    }
    private void displayStatus(LovGameController context) {
        LovBoard board = context.getBoard();

        // 1. HERO STATUS
        System.out.println(games.monstersandheroes.view.Colors.CYAN + "--- HERO SQUAD ---" + games.monstersandheroes.view.Colors.RESET);
        System.out.printf("%-15s | %-4s | %-6s | %-13s | %-3s | %-5s\n", "Name", "Lane", "Pos", "HP / MP", "Lvl", "Gold");
        System.out.println("----------------------------------------------------------------");

        for (ValorHero h : context.getHeroes()) {
            LovBoard.Position pos = board.getHeroPosition(h);
            String posStr = (pos != null) ? pos.row + "," + pos.col : "Dead";

            // Calculate Lane dynamically based on column
            String laneStr = "N/A";
            if (pos != null) {
                LovBoard.Lane lane = board.getLaneForColumn(pos.col);
                if (lane != null) laneStr = lane.getName();
            }

            System.out.printf("%-15s | %-4s | %-6s | %-5.0f / %-5.0f | %-3d | %-5.0f\n",
                    h.getName(),
                    laneStr,
                    posStr,
                    ceil(h.getHp()), ceil(h.getMana()),
                    h.getLevel(), h.getGold());
        }
        System.out.println("----------------------------------------------------------------");

        // 2. MONSTER THREATS (Optional Requirement)
        System.out.println(games.monstersandheroes.view.Colors.RED + "--- ENEMY THREATS ---" + games.monstersandheroes.view.Colors.RESET);

        // Helper map to track closest monster in Top/Mid/Bot
        java.util.Map<String, ValorMonster> closestThreats = new java.util.HashMap<>();
        java.util.Map<String, Integer> minDistance = new java.util.HashMap<>();

        // Scan all monsters
        for (java.util.Map.Entry<ValorMonster, LovBoard.Position> entry : board.getMonsterPositions().entrySet()) {
            ValorMonster m = entry.getKey();
            LovBoard.Position p = entry.getValue();

            // Get Lane
            LovBoard.Lane lane = board.getLaneForColumn(p.col);
            if (lane == null) continue;

            // Calculate Distance to Hero Nexus (Row 7)
            int dist = 7 - p.row;

            // Check if this is the closest one we've seen in this lane
            if (!minDistance.containsKey(lane.getName()) || dist < minDistance.get(lane.getName())) {
                minDistance.put(lane.getName(), dist);
                closestThreats.put(lane.getName(), m);
            }
        }

        if (closestThreats.isEmpty()) {
            System.out.println("No monsters on the board.");
        } else {
            System.out.printf("%-5s | %-15s | %-5s | %-6s\n", "Lane", "Closest Enemy", "HP", "Dist");
            for (String lane : new String[]{"Top", "Mid", "Bot"}) {
                if (closestThreats.containsKey(lane)) {
                    ValorMonster m = closestThreats.get(lane);
                    System.out.printf("%-5s | %-15s | %-5.0f | %-6d\n",
                            lane, m.getName(), ceil(m.getHp()), minDistance.get(lane));
                } else {
                    System.out.printf("%-5s | %-15s | %-5s | %-6s\n", lane, "Clear", "-", "-");
                }
            }
        }
        System.out.println("----------------------------------------------------------------\n");
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
        System.out.print(Colors.BLUE + "Equipped Weapons: " + Colors.RESET);
        java.util.List<core.model.item.Weapon> weapons = hero.getEquippedWeapon();

        if (weapons == null || weapons.isEmpty()) {
            System.out.println("None");
        } else {
            // Print all equipped weapons
            for (core.model.item.Weapon w : weapons) {
                System.out.print(w.getName() + " ");
            }
            System.out.println();
        }

        // Armor is still a single item
        System.out.println(Colors.BLUE + "Equipped Armor: " + Colors.RESET + (hero.getEquippedArmor() != null ? hero.getEquippedArmor().getName() : "None"));

        System.out.println(Colors.GREEN + "Spells: " +  Colors.RESET);
        for (Item item : hero.getInventory()) {
            if (item instanceof Spell)
            {System.out.print(" : " + item.getName());}
        }

        System.out.println();

        System.out.println(Colors.GREEN + "Potions: " + Colors.RESET);
        for (Item item : hero.getInventory()) {
            if (item instanceof Potion)
            {System.out.print(" : " + item.getName());}
        }

    }
}