package games.legendsofvalors.controller;

import core.interfaces.GameEngine;
import games.legendsofvalors.model.ValorHero;

public class LovGameController implements GameEngine {

    @Override
    public void startGame() {
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

    private void demoBoardSetup() {
        List<ValorHero> heroes = new ArrayList<ValorHero>();
        heroes.add(new ValorHero("DemoWarrior", 100, 100, 100, 100, 500, 0));
        heroes.add(new ValorHero("DemoSorcerer", 120, 80, 90, 110, 450, 0));
        heroes.add(new ValorHero("DemoPaladin", 110, 95, 85, 105, 470, 0));

        int[][] heroSlots = new int[][]{
                {7, 0},
                {7, 3},
                {7, 6}
        };

        for (int i = 0; i < heroes.size(); i++) {
            ValorHero hero = heroes.get(i);
            int row = heroSlots[i][0];
            int col = heroSlots[i][1];
            hero.setHomeNexus(row, col, laneNameForColumn(col));
            board.placeHero(hero, row, col);
        }

        List<ValorMonster> monsters = new ArrayList<ValorMonster>();
        monsters.add(new ValorMonster("DemoDragon", 1, 120, 50, 15, laneNameForColumn(0)));
        monsters.add(new ValorMonster("DemoSpirit", 1, 90, 40, 20, laneNameForColumn(3)));
        monsters.add(new ValorMonster("DemoExo", 1, 100, 60, 10, laneNameForColumn(6)));

        int[][] monsterSlots = new int[][]{
                {0, 0},
                {0, 3},
                {0, 6}
        };

        for (int i = 0; i < monsters.size(); i++) {
            board.placeMonster(monsters.get(i), monsterSlots[i][0], monsterSlots[i][1]);
        }
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
}