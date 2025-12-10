package games.legendsofvalors.controller;

import core.interfaces.GameEngine;
import games.legendsofvalors.model.ValorHero;

public class LovGameController implements GameEngine {

    @Override
    public void startGame() {
        System.out.println("--- LEGENDS OF VALOR ---");

        // TEST: Create a generic hero
        ValorHero testHero = new ValorHero("TestWarrior", 100, 100, 100, 100, 500, 0);

        // TEST: Assign them a lane
        testHero.setHomeNexus(7, 0, "Top");

        // TEST: Verify the new field works
        System.out.println("Hero Created: " + testHero.getName());
        System.out.println("Assigned Lane: " + testHero.getLane());

        testHero.recall();
    }
}