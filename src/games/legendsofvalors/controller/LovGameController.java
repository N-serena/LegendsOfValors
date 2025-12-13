package games.legendsofvalors.controller;

import core.interfaces.FightStrategy;
import core.interfaces.GameEngine;
import core.model.Party;
import core.model.entity.Hero;
import core.model.item.Weapon;
import core.interfaces.Board;
import games.commoncontrollers.GameController;
import games.commoncontrollers.InventoryController;
import games.commoncontrollers.MarketController;
import core.model.market.Market;
import games.commoncontrollers.PartyController;
import games.commoncontrollers.actions.Attack;
import games.legendsofvalors.controller.battle.LoVBattleProxy;
import games.legendsofvalors.interfaces.Battle;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.model.world.LovBoard;
import games.legendsofvalors.model.world.LovTile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class LovGameController extends GameController implements GameEngine {

    Scanner scanner;
    LovBoard board;
    InventoryController inventoryController;
    MarketController marketController;
    PartyController partyController;

    //for testing
    public static List<ValorMonster> monsters = new ArrayList<ValorMonster>();

    public LovGameController() {
        board = new LovBoard();
    }

    @Override
public void startGame() {
        partyController = new PartyController();
        inventoryController = new InventoryController(scanner);
    scanner = new Scanner(System.in);
    System.out.println("--- LEGENDS OF VALOR ---");
    demoBoardSetup();
    System.out.println(board.renderColored());
    System.out.println("Press 'L' to view map instructions/legend.");
    promptLegend();
}

private void promptLegend() {
    String input = scanner.nextLine();
    if (input != null && input.trim().equalsIgnoreCase("L")) {
        System.out.println(board.getLegendText());
    }
}

private void demoBoardSetup(){
    List<ValorHero> heroes = new ArrayList<ValorHero>();
    ValorHero h1 = new ValorHero("DemoWarrior", 100, 100, 100, 100, 500, 0);
    ValorHero h2 = new ValorHero("DemoSorcerer", 120, 80, 90, 110, 450, 0);
    ValorHero h3 = new ValorHero("DemoPaladin", 110, 95, 85, 105, 470, 0);


    party.addHero(h1);
    party.addHero(h2);
    party.addHero(h3);

    int[][] heroSlots = new int[][]{
            {7, 0},
            {7, 3},
            {7, 6}
    };

    // TEST: Create a generic hero
    for (int i = 0; i < party.getHeroes().size(); i++) {
        ValorHero hero = (ValorHero) party.getHeroes().get(i);
        int row = heroSlots[i][0];
        int col = heroSlots[i][1];
        hero.setHomeNexus(row, col, laneNameForColumn(col));
        board.placeHero(hero, row, col);
    }

    // TEST: Assign them a lane
    monsters.add(new ValorMonster("DemoDragon", 1, 120, 50, 15, laneNameForColumn(0)));
    monsters.add(new ValorMonster("DemoSpirit", 1, 90, 40, 20, laneNameForColumn(3)));
    monsters.add(new ValorMonster("DemoExo", 1, 100, 60, 10, laneNameForColumn(6)));

    // TEST: Verify the new field works

    int[][] monsterSlots = new int[][]{
            {0, 0},
            {0, 3},
            {0, 6}
    };

    for (int i = 0; i < monsters.size(); i++) {
        board.placeMonster(monsters.get(i), monsterSlots[i][0], monsterSlots[i][1]);
    }

    //test for attacking and casting spell
    Weapon w = new Weapon("Sword", 100, 100, 100, 2);
    inventoryController.equipItem(party.getHeroes().get(0), w);
    FightStrategy attack = new Attack();

    Battle b = new LoVBattleProxy();
    b.startBattle(party.getHeroes().get(0), attack, party, (Board) board);
    System.out.println(monsters.get(0).getHp());

    try {
        loadGameData();
    } catch (IOException e) {
        e.printStackTrace();
    }

    //Testing Market
    String choice = scanner.nextLine();
    if (choice.equals("M"))
    {
        initializeMarket((ValorHero) party.getHeroes().get(0));
    }

    scanner.nextLine();

    //Testing Inventory
    inventoryController.openEquippedWeaponsMenu(party.getHeroes().get(0));
    inventoryController.openPotionMenu(party.getHeroes().get(0));
    inventoryController.openSpellsMenu(party.getHeroes().get(0));

}

private String laneNameForColumn(int column) {
    if (column == 0 || column == 1) {
        return "Top";
    }
    if (column == 3 || column == 4) {
        return "Mid";
    }
    return "Bot";
}

public LovBoard getBoard() {
    return board;
}

    public void initializeMarket(ValorHero hero)
    {
        LovBoard.Position currentPosition = board.getHeroPosition(hero);

        LovTile tile = board.getTiles()[currentPosition.row][currentPosition.col];
        //LovTile tile = board.getTiles()[7][1];

        //if hero is on the nexus tile, and chooses to go to a market, load market
        if(board.getTiles()[currentPosition.row][currentPosition.col].isHeroNexus())
        {
            marketController = new MarketController(scanner, inventoryController);
            marketController.handleShopper(hero, new Market(allItems));
        }
        else
        {
            System.out.println("You can only enter the market at your home nexus!");
        }
    }
}