package games.legendsofvalors.controller;

import core.interfaces.GameEngine;
import core.model.GameDatabase;
import core.model.Party;
import core.model.entity.Hero;
import core.model.entity.Monster;
import core.util.Colors;
import games.commoncontrollers.GameController;
import games.commoncontrollers.InputHandler;
import games.commoncontrollers.InventoryController;
import games.commoncontrollers.MarketController;
import games.legendsofvalors.states.HeroTurnState;
import games.legendsofvalors.interfaces.LovGameState;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.model.world.LovBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LovGameController extends GameController implements GameEngine {

    private LovBoard board;
    private Scanner scanner;
    private LovGameState currentState;
    private int roundNumber = 1;
    public boolean isRunning = true;
    public int numOfMonstersSpawned;

    //Controllers
    private InputHandler inputHandler;
    private MarketController marketController;

    public LovGameController() {
        super(); // Initializes 'party', 'allHeroes', etc. from parent
        this.board = new LovBoard();
        this.scanner = new Scanner(System.in);

        //Initialize controllers
        this.inputHandler = new InputHandler();
        this.inventoryController = new InventoryController(scanner);
        this.marketController = new MarketController(scanner, inventoryController);
        numOfMonstersSpawned = 0;
    }

    @Override
    public void startGame() {
        // 1. Clear Screen & Show Title
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println(Colors.PURPLE + "========================================");
        System.out.println("       L E G E N D S   O F   V A L O R       ");
        System.out.println("========================================" + Colors.RESET);

        //System.out.println("Loading game assets...");
        GameDatabase.getInstance().loadData();

        // 2. Show Rules/Legend IMMEDIATELY (No prompt)
        System.out.println("\n" + board.getLegendText());

        System.out.println(Colors.YELLOW + "\n--- MISSION ---" + Colors.RESET);
        System.out.println("1. Move your heroes to the Monster Nexus (Row 0) to win.");
        System.out.println("2. Defeat monsters to gain XP and Gold.");
        System.out.println("3. Buy better gear at your Nexus (Row 7).");
        System.out.println("4. Do not let monsters reach your Nexus!");
        //System.out.println(board.renderColored());

        // 3. Wait for Enter
        System.out.println(Colors.GREEN + "\nPress ENTER to start the battle..." + Colors.RESET);
        inputHandler.enter(); // Wait for actual enter

        // 4. Setup & Start Loop
        setupGame();
        this.currentState = new HeroTurnState();

        while (isRunning) {
            inputHandler.enter();
            currentState.execute(this);
        }
    }

    private void setupGame() {
        System.out.println("--- FORM YOUR PARTY ---");
        System.out.println("You must select 3 Heroes to enter the Legends of Valor.");

        // 1. Get All Available Heroes from Database
        List<Hero> allHeroes = GameDatabase.getInstance().getAllHeroes();

        // 2. Select 3 Heroes (One for each lane)
        for (int i = 1; i <= 3; i++) {
            System.out.println("\nSelect Hero #" + i + " (" + getLaneName(getColForHeroIndex(i-1)) + " Lane):");
            Hero selected = selectHero(allHeroes);

            Hero valorHero = new ValorHero(selected);
            party.addHero(valorHero);
        }

        getComplimentaryWeapons();

        // 3. Place Heroes on Board
        // Indices 0, 1, 2 correspond to Top, Mid, Bot
        int[][] slots = {{7, 0}, {7, 3}, {7, 6}};

        for (int i = 0; i < party.getSize(); i++) {
            ValorHero h = (ValorHero) party.getHero(i);
            int r = slots[i][0];
            int c = slots[i][1];

            String lane = getLaneName(c);
            h.setHomeNexus(r, c, lane);

            if (board.placeHero(h, r, c)) {
                System.out.println(h.getName() + " entered the " + lane + " Lane.");
            }
        }

        // 4. Spawn Initial Monsters (Keep existing logic)
        List<Monster> templates = GameDatabase.getInstance().getAllMonsters();
        if (!templates.isEmpty()) {
            spawnMonster(templates.get(0), 0, 0, "Top");
            spawnMonster(templates.get(1), 0, 3, "Mid");
            spawnMonster(templates.get(2), 0, 6, "Bot");
        }
    }

    private Hero selectHero(List<Hero> options) {
        System.out.printf("%-4s %-20s %-10s %-5s\n", "ID", "Name", "Type", "Lvl");
        System.out.println("------------------------------------------------");

        for (int i = 0; i < options.size(); i++) {
            Hero h = options.get(i);
            // Check if already selected
            boolean taken = false;
            for (int p = 0; p < party.getSize(); p++) {
                if (party.getHero(p).getName().equals(h.getName())) taken = true;
            }

            if (taken) {
                System.out.printf("%-4d %-20s [ALREADY SELECTED]\n", (i + 1), h.getName());
            } else {
                System.out.printf("%-4d %-20s %-10s %-5d\n", (i + 1), h.getName(), h.getClass().getSimpleName(), h.getLevel());
            }
        }

        while (true) {
            System.out.print("Enter ID: ");
            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                if (choice > 0 && choice <= options.size()) {
                    Hero h = options.get(choice - 1);
                    // Prevent duplicates
                    boolean taken = false;
                    for (int p = 0; p < party.getSize(); p++) {
                        if (party.getHero(p).getName().equals(h.getName())) taken = true;
                    }
                    if (taken) {
                        System.out.println("Hero already in party!");
                    } else {
                        return h;
                    }
                } else {
                    System.out.println("Invalid ID.");
                }
            } else {
                scanner.next();
            }
        }
    }

    private int getColForHeroIndex(int idx) {
        if (idx == 0) return 0; // Top
        if (idx == 1) return 3; // Mid
        return 6; // Bot
    }

    private void spawnMonster(Monster template, int r, int c, String lane) {
        ValorMonster vm = new ValorMonster(template, lane);
        vm.scaleStats(1);
        board.placeMonster(vm, r, c);
        numOfMonstersSpawned++;
    }

    private String getLaneName(int col) {
        if (col <= 1) return "Top";
        if (col >= 3 && col <= 4) return "Mid";
        return "Bot";
    }

    // --- Getters for States ---
    public void setState(LovGameState newState) { this.currentState = newState; }
    public LovBoard getBoard() { return board; }
    public Scanner getScanner() { return scanner; }
    public Party getParty() { return party; }

    // Helper
    public List<ValorHero> getHeroes() {
        List<ValorHero> vh = new ArrayList<>();
        for (Hero h : party.getHeroes()) {
            if (h instanceof ValorHero) vh.add((ValorHero) h);
        }
        return vh;
    }

    public void  displayEndGameStats(String status)
    {
        System.out.println("The heroes have " + status + " the game against the monsters.");
        System.out.println(" + NUMBER OF ROUNDS PLAYED: " + roundNumber);
        System.out.println("Hero Stats: ");
        for(Hero h : getHeroes())
        {
            System.out.println(h);
        }
        System.out.println();
        System.out.println("NUMBER OF MONSTERS DEFEATED: " + (numOfMonstersSpawned - board.getNumMonsters()));
        System.out.println("------------------------------------------------------------------");

    }

    public void displayBoard()
    {
        System.out.println();
        System.out.println(board.renderColored());
    }

    public int getRoundNumber() { return roundNumber; }
    public void incrementRound() { this.roundNumber++; }
    public InventoryController getInventoryController() { return inventoryController; }
    public MarketController getMarketController() { return marketController; }
}