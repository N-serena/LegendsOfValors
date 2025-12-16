package games.legendsofvalors.controller;

import games.commoncontrollers.HeroController;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.world.LovBoard;

/**
 * Hero controller for valor hero
 */
public class LoVHeroController extends HeroController {

    public LoVHeroController()
    {}

    /**
     * Logic to respawn the hero at their nexus if they were defeated
     */
    public void respawnHero(ValorHero hero, LovBoard board) {
        int nexusRow = hero.getNexusRow();
        int nexusCol = hero.getNexusCol();

        hero.regenerateStats();

        //move hero to their nexus
        board.moveHero(hero, nexusRow, nexusCol);
    }

}
