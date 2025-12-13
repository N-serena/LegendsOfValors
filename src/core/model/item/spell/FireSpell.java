package core.model.item.spell;

import core.model.entity.Monster;
import core.util.GameConfig;

/**
 * subclass for a type of spell - fire, affects the defense of the monster
 * * @author Chris Mary Benson.
 * @version 1.0
 */

public class FireSpell extends Spell {

    public FireSpell(String name, double price, int minLevel, double damage, double manaCost, SpellType type)
    {
        super(name, price, minLevel, damage, manaCost, type);
    }

    @Override
    public void castEffect(Monster target)
    {
        target.setDefense(target.getDefense() * getEffect());
    }

    @Override
    public double getEffect()
    {
        return GameConfig.FIRESPELLDAMAGE;
    }

}
