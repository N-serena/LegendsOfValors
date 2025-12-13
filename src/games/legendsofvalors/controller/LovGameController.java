package games.legendsofvalors.controller;

import core.interfaces.GameEngine;
import core.model.GameDatabase;
import core.model.Party;
import core.model.entity.Hero;
import core.model.entity.Monster;
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
        System.out.println("--- LEGENDS OF VALOR ---");
        // 1. Load Data
        GameDatabase.getInstance().loadData();
        // 2. Setup Board & Party
        setupGame();
        // 3. Start State Loop
        this.currentState = new HeroTurnState();

        System.out.println("Press 'L' to view map instructions/legend.");
        promptLegend();

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
        vm.scaleStats(1); // Scale to level 1
        board.placeMonster(vm, r, c);
    }

    private String getLaneName(int col) {
        if (col <= 1) return "Top";
        if (col >= 3 && col <= 4) return "Mid";
        return "Bot";
    }

    private void promptLegend() {
        String input = scanner.nextLine();
        if (input != null && input.trim().equalsIgnoreCase("L")) {
            System.out.println(board.getLegendText());
        }
    }

    // --- Getters for States ---
    public void setState(LovGameState newState) { this.currentState = newState; }
    public LovBoard getBoard() { return board; }
    public Scanner getScanner() { return scanner; }
    public Party getParty() { return party; }

    // Helper: Converts Party to List<ValorHero> for Logic compatibility
    public List<ValorHero> getHeroes() {
        List<ValorHero> vh = new ArrayList<>();
        for (Hero h : party.getHeroes()) {
            if (h instanceof ValorHero) vh.add((ValorHero) h);
        }
        return vh;
    }

    public int getRoundNumber() { return roundNumber; }
    public void incrementRound() { this.roundNumber++; }

    // Getters for Controllers (needed for Market/Inventory states)
    public InventoryController getInventoryController() { return inventoryController; }
    public MarketController getMarketController() { return marketController; }
}