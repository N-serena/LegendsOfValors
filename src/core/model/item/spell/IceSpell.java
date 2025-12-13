package core.model.item.spell;

import core.model.entity.Monster;
import core.util.GameConfig;

/**
 * subclass for a type of spell - ice, affects the base damage of the monster
 * * @author Chris Mary Benson.
 * @version 1.0
 */

public class IceSpell extends Spell {

    public IceSpell(String name, double price, int minLevel, double damage, double manaCost, SpellType type)
    {
        super(name, price, minLevel, damage, manaCost, type);
    }

    @Override
    public void castEffect(Monster target)
    {
        target.setBaseDamage(target.getBaseDamage() * GameConfig.ICESPELLDAMAGE);
    }

    @Override
    public double getEffect()
    {
        return GameConfig.ICESPELLDAMAGE;
    }
}
