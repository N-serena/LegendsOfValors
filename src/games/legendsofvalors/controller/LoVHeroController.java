package games.legendsofvalors.controller;

import games.commoncontrollers.HeroController;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.world.LovBoard;

public class LoVHeroController extends HeroController {

    public LoVHeroController()
    {}

    public void respawnHero(ValorHero hero, LovBoard board) {
        int nexusRow = hero.getNexusRow();
        int nexusCol = hero.getNexusCol();

        hero.regenerateStats();

        //can use teleport here but for now use moveHero
        board.moveHero(hero, nexusRow, nexusCol);
    }

}
