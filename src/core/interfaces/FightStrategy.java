package core.interfaces;

import core.model.entity.LivingEntity;
import core.model.item.Item;

/**
 * An interface for fight strategies, implementing the fight strategy pattern
 * * @author Chris Mary Benson.
 * @version 1.0
 */

//action consists of two types -> attack and casting a spell (common between both games)
public interface FightStrategy {
    public void performFightAction(LivingEntity attacker, LivingEntity target, Item item);
}
