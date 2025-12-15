package core.model.entity.decorator;

import core.model.entity.Hero;
import core.model.item.Item;

import java.util.List;

/**
 * A hero decorator class that acts as a base for
 * adding extra functionalities to the base Hero without modifying Hero or its subclasses
 */
public abstract class HeroDecorator extends Hero {
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

    @Override
    public int getLevel()
    {
        return this.hero.getLevel();
    }

    @Override
    public double getGold()
    {
        return this.hero.getGold();
    }

    @Override
    public double getMana()
    {
        return this.hero.getMana();
    }

    @Override
    public String getName()
    {
        return this.hero.getName();
    }
}
