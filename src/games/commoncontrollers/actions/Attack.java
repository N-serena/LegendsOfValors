package games.commoncontrollers.actions;

import core.interfaces.FightStrategy;
import games.commoncontrollers.HeroController;
import core.model.entity.Hero;
import core.model.entity.LivingEntity;
import core.model.entity.Monster;
import core.model.item.Item;
import core.model.item.Weapon;

/**
 * A type of fight strategy - attack
 * * @author Chris Mary Benson.
 * @version 1.0
 */

public class Attack implements FightStrategy {

    @Override
    public void performFightAction(LivingEntity attacker, LivingEntity target, Item item)
    {
        HeroController heroController = new HeroController();

        if (attacker instanceof Hero)
        {
            Monster monster = (Monster) target;

            if (Math.random() < monster.getDodgeChance() * 0.01) {
                System.out.println(target.getName() + " dodged the attack from " + attacker.getName() + "!");
            } else {
                double damage = heroController.calculateDamage((Hero) attacker, (Weapon) item);
                double actualDmg = Math.max(0, damage - (monster.getDefense() * 0.02));
                target.takeDamage(actualDmg);
                System.out.println(attacker.getName() + " dealt " + (int)actualDmg + " damages to " + target.getName() + ".");
            }

        }

        else if (attacker instanceof Monster)
        {
            Hero hero = (Hero) target;
            Monster monster = (Monster) attacker;

            double dodgeChance = heroController.calculateDodgeChance(hero);

            if (Math.random() < dodgeChance) {
                System.out.println(attacker.getName() + " attacked " + target.getName()
                        + " -> BUT MISSED! " + target.getName() + " dodged the attack!");
            } else {
                double incomingDmg = monster.getBaseDamage();

                // Armor reduction logic
                // (This logic is simple enough to stay here, or you could move 'calculateDefense' to HeroController too)
                double defense = (hero.getEquippedArmor() != null) ? hero.getEquippedArmor().getDamageReduction() : 0;
                double actualDmg = Math.max(0, incomingDmg - defense);

                target.takeDamage(actualDmg);

                if (actualDmg == 0 && defense > 0) {
                    System.out.println(monster.getName() + " attacked " + target.getName() + " -> BLOCKED by Armor!");
                } else {
                    System.out.println(monster.getName() + " hit " + target.getName() + " for " + (int)actualDmg + " damages.");
                }
            }
        }
    }
}
