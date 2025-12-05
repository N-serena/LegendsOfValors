package controller;

import model.Party;
import model.entity.Hero;
import model.entity.Monster;
import model.item.Item;
import model.world.Board;
import model.world.Tile;
import util.GameConfig;
import util.GameDataParser;
import util.SoundPlayer;
import view.Colors;
import view.GameView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * The Main Controller for the Game Loop.
 * Handles initialization, roaming state
 * Orchestrates the Game Loop and delegates sub-tasks to specialized controllers.
 * * @author Serena N.
 * @version 3.0 (Refactored)
 */
public class GameController {
    private Scanner scanner;
    private Party party;
    private Board board;
    private GameView view;

    // Data Pools
    private List<Hero> allHeroes;
    private List<Item> allItems;
    private List<Monster> allMonsters;

    // Sub-Controllers
    private MarketController marketController;
    private InventoryController inventoryController;
    private GuildController guildController;
    private HeroController heroController;

    public GameController() {
        this.scanner = new Scanner(System.in);
        this.party = new Party();
        this.allHeroes = new ArrayList<>();
        this.allMonsters = new ArrayList<>();
        this.allItems = new ArrayList<>();
        this.view = new GameView();

        // Initialize Logic Delegates
        this.inventoryController = new InventoryController(this.scanner);
        this.marketController = new MarketController(this.scanner, this.inventoryController);
        this.guildController = new GuildController(this.scanner);
        this.heroController = new HeroController();
    }

    public void start() {
        //0. Start audio (optional)
        SoundPlayer.playBackgroundMusic("theme.wav");

        // 1. Visuals
        view.printTitleScreen();
        view.printIntroStory();
        view.printRules();
        System.out.println("\nPress ENTER to begin your adventure...");
        try { System.in.read(); } catch (Exception e) {}

        // 2. Load Data
        try {
            loadGameData(); // Refactored into helper method

            // Create Board with Global Items
            this.board = new Board(GameConfig.BOARD_WIDTH, GameConfig.BOARD_HEIGHT, allItems);

        } catch (IOException e) {
            System.err.println("Critical Error loading files: " + e.getMessage());
            return;
        }

        // 3. Setup
        selectParty();
        gameLoop();
    }

    private void loadGameData() throws IOException {
        System.out.println("Loading game assets...");
        allHeroes.addAll(GameDataParser.parseHeroes("Warriors.txt", "Warrior"));
        allHeroes.addAll(GameDataParser.parseHeroes("Sorcerers.txt", "Sorcerer"));
        allHeroes.addAll(GameDataParser.parseHeroes("Paladins.txt", "Paladin"));

        allMonsters.addAll(GameDataParser.parseMonsters("Dragons.txt", "Dragon"));
        allMonsters.addAll(GameDataParser.parseMonsters("Exoskeletons.txt", "Exoskeleton"));
        allMonsters.addAll(GameDataParser.parseMonsters("Spirits.txt", "Spirit"));

        allItems.addAll(GameDataParser.parseWeapons("Weaponry.txt"));
        allItems.addAll(GameDataParser.parseArmor("Armory.txt"));
        allItems.addAll(GameDataParser.parsePotions("Potions.txt"));
        allItems.addAll(GameDataParser.parseSpells("IceSpells.txt", model.item.Spell.SpellType.ICE));
        allItems.addAll(GameDataParser.parseSpells("FireSpells.txt", model.item.Spell.SpellType.FIRE));
        allItems.addAll(GameDataParser.parseSpells("LightningSpells.txt", model.item.Spell.SpellType.LIGHTNING));
    }

    private void selectParty() {
        System.out.println("\n--- HERO SELECTION ---");
        int count = 0;
        while (count < 1 || count > 3) {
            System.out.print("Enter party size (1-3): ");
            if (scanner.hasNextInt()) count = scanner.nextInt();
            else scanner.next();
        }

        // Display Options
        System.out.printf("%-4s %-20s %-10s\n", "ID", "Name", "Type");
        for (int i = 0; i < allHeroes.size(); i++) {
            System.out.printf("%-4d %-20s %-10s\n", (i+1), allHeroes.get(i).getName(), allHeroes.get(i).getClass().getSimpleName());
        }

        for (int i = 1; i <= count; i++) {
            int choice = -1;
            while (choice < 1 || choice > allHeroes.size()) {
                System.out.print("Select Hero " + i + ": ");
                if (scanner.hasNextInt()) choice = scanner.nextInt();
                else scanner.next();
            }
            party.addHero(allHeroes.get(choice - 1));
        }
        System.out.println("Party assembled!");
    }

    private void gameLoop() {
        boolean running = true;
        while (running) {
            // 1. Print World
            view.printBoard(board, party);
            // 2. Print Controls (Formatted)
            System.out.println("----------------- CONTROLS -----------------");
            System.out.println("  W/A/S/D : Move (Up/Left/Down/Right)");
            System.out.println("  M       : Enter Market");
            System.out.println("  I       : Info (Stats, Equip, Potions)");
            System.out.println("  G       : Guild (Swap Heroes)");
            System.out.println("  C       : Cheat Code (Use this to level up easily and testing)");
            System.out.println("  Q       : Quit Game");
            System.out.print("> ");

            String input = scanner.next().toUpperCase();
            switch (input) {
                case "W": move(0, -1); break;
                case "A": move(-1, 0); break;
                case "S": move(0, 1); break;
                case "D": move(1, 0); break;
                case "Q": running = false; break;
                // DELEGATE: Info/Inventory
                case "I": handleInfoMenu(); break;
                // DELEGATE: Market
                case "M": handleMarket();break;
                // DELEGATE: Guild
                case "G":
                    guildController.enterGuild(party, allHeroes);
                    break;
                // Cheat Code for testing
                case "C":
                    for(Hero h : party.getHeroes()) {
                        h.addGold(1000);
                        // Use Controller to level up
                        // Note: This updates the stats but doesn't check "Game Over" inside the method
                        heroController.gainExperience(h, h.getLevel() * 10 + 1);
                    }
                    System.out.println("CHEAT: Resources Added.");
                    // Verify if the cheat triggered the win condition immediately
                    for(Hero h : party.getHeroes()) {
                        if (h.getLevel() > GameConfig.MAX_LEVEL) {
                            System.out.println(Colors.YELLOW + "\nCHEAT CODE TRIGGERED VICTORY!" + Colors.RESET);
                            System.out.println("***************************************************");
                            System.out.println("                CONGRATULATIONS!                   ");
                            System.out.println("***************************************************");
                            System.exit(0);
                        }
                    }
                    break;

                default: System.out.println("Invalid input.");
            }
        }
    }

    private void handleMarket() {
        Tile t = board.getTile(party.getX(), party.getY());
        if (t instanceof model.world.MarketTile) {
            marketController.enterMarket(((model.world.MarketTile) t).getMarket(), party);
        } else {
            System.out.println("Not at a market.");
        }
    }

    private void showPartyStats() {
        System.out.println("\n================== PARTY STATUS ==================");

        for (int i = 0; i < party.getSize(); i++) {
            Hero h = party.getHero(i);

            System.out.printf(" %d. %-15s (Lvl %d %s)\n",
                    (i + 1), h.getName(), h.getLevel(), h.getClass().getSimpleName());

            // Stats Grid
            System.out.printf("    HP: %-5.0f/ %-5.0f | MP: %-5.0f\n",
                    h.getHp(), (double)(h.getLevel() * 100), h.getMana());

            System.out.printf("    Str: %-4.0f | Dex: %-4.0f | Agi: %-4.0f\n",
                    h.getStrength(), h.getDexterity(), h.getAgility());

            System.out.printf("    Gold: %-5.0f | XP: %-5.0f\n",
                    h.getGold(), h.getExperience());

            // COMBAT STATS
            System.out.println("    Combat Power");

            // Calculate Effective Defense from Armor
            double armorDef = (h.getEquippedArmor() != null) ? h.getEquippedArmor().getDamageReduction() : 0;

            // Calculate Total Damage (Strength + Weapon) using the Hero's logic method
            double totalDmg = heroController.calculateDamage(h);

            // Calculate Dodge %
            double dodgeChance = heroController.calculateDodgeChance(h) * 100;

            System.out.printf("     Damage:  %-5.0f (Str + Weapon)\n", totalDmg);
            System.out.printf("     Defense: %-5.0f (Armor)\n", armorDef);
            System.out.printf("     Dodge:   %-5.0f%%\n", dodgeChance);

            // Gear
            System.out.println("    Equipped Gear");
            if (h.getEquippedWeapon() != null) {
                System.out.printf("     Weapon: %s (Val: %.0f)\n", h.getEquippedWeapon().getName(), h.getEquippedWeapon().getDamage());
            } else {
                System.out.println("     Weapon: None");
            }

            if (h.getEquippedArmor() != null) {
                System.out.printf("     Armor:  %s (Val: %.0f)\n", h.getEquippedArmor().getName(), h.getEquippedArmor().getDamageReduction());
            } else {
                System.out.println("     Armor:  None");
            }

            // Inventory Listing
            System.out.println("    Inventory:");
            if (h.getInventory().isEmpty()) {
                System.out.println("      (Empty)");
            } else {
                for (model.item.Item item : h.getInventory()) {
                    // Print Item Name and Level requirement
                    System.out.printf("      - %-18s (Lvl %d)\n", item.getName(), item.getMinLevel());
                }
            }

            if (i < party.getSize() - 1) {
                System.out.println(" --------------------------------------------------");
            }
        }
        System.out.println("==================================================");
    }

    private void handleInfoMenu() {
        for (int i = 0; i < party.getSize(); i++) {
            System.out.println("HERO #" + (i + 1));
            showPartyStats();
        }
        System.out.println("1. Equip Item \n 2. Use Potion \n 0. Back");
        if (scanner.hasNextInt()) {
            int choice = scanner.nextInt();
            if (choice == 1 || choice == 2) {
                System.out.print("Enter Hero ID: ");
                int id = scanner.nextInt();
                if (id > 0 && id <= party.getSize()) {
                    if (choice == 1) inventoryController.openEquipMenu(party.getHero(id - 1));
                    else inventoryController.openPotionMenu(party.getHero(id - 1));
                }
            }
        } else scanner.next();
    }

    private void move(int dx, int dy) {
        int newX = party.getX() + dx;
        int newY = party.getY() + dy;

        Tile target = board.getTile(newX, newY);
        if (target == null) { System.out.println("Blocked: Border."); return; }
        if (target.getType().equals("Inaccessible")) { System.out.println("Blocked: Wall."); return; }

        party.setLocation(newX, newY);
        target.enter();

        if (target.getType().equals("Common")) {
            double roll = Math.random();
            if (roll < GameConfig.CHANCE_LOOT) {
                findRandomLoot();
            } else if (roll < GameConfig.CHANCE_BATTLE) {
                // Pass scanner to battle controller to reuse input stream
                BattleController battle = new BattleController(this.scanner, this.inventoryController, this.heroController);
                battle.startBattle(party, allMonsters);
            }
        }
    }

    private void findRandomLoot() {
        System.out.println(Colors.YELLOW + "\n✨ LUCKY FIND! A chest! ✨" + Colors.RESET);
        Random rand = new Random();
        if (rand.nextBoolean() && !allItems.isEmpty()) {
            Item loot = allItems.get(rand.nextInt(allItems.size()));
            System.out.println("Found: " + loot.getName());
            party.getHero(0).addItem(loot);
        } else {
            int gold = party.getHero(0).getLevel() * 500;
            System.out.println("Found: " + gold + " Gold!");
            for (Hero h : party.getHeroes()) h.addGold(gold);
        }
    }
}