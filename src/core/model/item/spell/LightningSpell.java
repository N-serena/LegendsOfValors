package core.model.item.spell;

import core.model.entity.Monster;
import core.util.GameConfig;

/**
 * subclass for a type of spell - lightning, affects the dodge ability of the monster
 * * @author Chris Mary Benson.
 * @version 1.0
 */

public class LightningSpell extends Spell {

    public LightningSpell(String name, double price, int minLevel, double damage, double manaCost, SpellType type)
    {
        super(name, price, minLevel, damage, manaCost, type);
    }

    @Override
    public void castEffect(Monster target)
    {
        target.setDodgeChance(target.getDodgeChance() * GameConfig.LIGHTNINGSPELLDAMAGE);
    }

    @Override
    public double getEffect()
    {
        return GameConfig.LIGHTNINGSPELLDAMAGE;
    }
}
