package games.legendsofvalors.model;

import core.model.entity.Hero;

/**
 * A hero decorator class used for adding extra functionalities to the base Hero without modifying Hero or its subclasses
 */
public class HeroDecorator extends Hero {
    protected Hero hero;

    public HeroDecorator(Hero hero) {
        super(hero.getName(), hero.getMana(), hero.getStrength(), hero.getAgility(), hero.getDexterity(), hero.getGold(), hero.getExperience());
        this.hero = hero;
    }

    @Override
    public void levelUp()
    {
        this.hero.levelUp();
    }
}
