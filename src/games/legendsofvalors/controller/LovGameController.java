package games.legendsofvalors.controller;

import core.interfaces.GameEngine;
import core.model.GameDatabase;
import core.model.Party;
import core.model.entity.Hero;
import core.model.entity.Monster;
import core.util.Colors;
import core.util.SoundPlayer;
import games.commoncontrollers.GameController;
import games.commoncontrollers.InventoryController;
import games.commoncontrollers.MarketController;
import games.commoncontrollers.PartyController;
import games.legendsofvalors.states.HeroTurnState;
import games.legendsofvalors.states.LovGameState;
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
    private boolean isRunning = true;

    //Controllers
    private MarketController marketController;
    private PartyController partyController;

    public LovGameController() {
        super(); // Initializes 'party', 'allHeroes', etc. from parent
        this.board = new LovBoard();
        this.scanner = new Scanner(System.in);

        //Initialize controllers
        this.inventoryController = new InventoryController(scanner);
        this.marketController = new MarketController(scanner, inventoryController);
        this.partyController = new PartyController();
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
        //scanner.nextLine(); // Consume previous newline if any
        if (scanner.hasNextLine()) scanner.nextLine(); // Wait for actual enter

        // 4. Setup & Start Loop
        setupGame();
        this.currentState = new HeroTurnState();

        while (isRunning) {
            System.out.println(board.renderColored());
            currentState.execute(this);
        }
    }

    private void setupGame() {
        // Create Heroes
        ValorHero h1 = new ValorHero("DemoWarrior", 100, 100, 100, 100, 500, 0);
        ValorHero h2 = new ValorHero("DemoSorcerer", 120, 80, 90, 110, 450, 0);
        ValorHero h3 = new ValorHero("DemoPaladin", 110, 95, 85, 105, 470, 0);

        // Add to the 'party' object (Inherited from GameController)
        party.addHero(h1);
        party.addHero(h2);
        party.addHero(h3);

        // Place Heroes on Board
        int[][] slots = {{7, 0}, {7, 3}, {7, 6}};
        for (int i = 0; i < party.getSize(); i++) {
            ValorHero h = (ValorHero) party.getHero(i);
            int r = slots[i][0];
            int c = slots[i][1];
            h.setHomeNexus(r, c, getLaneName(c));
            board.placeHero(h, r, c);
        }

        // Spawn Initial Monsters
        List<Monster> templates = GameDatabase.getInstance().getAllMonsters();
        if (!templates.isEmpty()) {
            spawnMonster(templates.get(0), 0, 0, "Top");
            spawnMonster(templates.get(1), 0, 3, "Mid");
            spawnMonster(templates.get(2), 0, 6, "Bot");
        }
    }

    private void spawnMonster(Monster template, int r, int c, String lane) {
        ValorMonster vm = new ValorMonster(template, lane);
        vm.scaleStats(1);
        board.placeMonster(vm, r, c);
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

    public int getRoundNumber() { return roundNumber; }
    public void incrementRound() { this.roundNumber++; }
    public InventoryController getInventoryController() { return inventoryController; }
    public MarketController getMarketController() { return marketController; }
}