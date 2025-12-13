package games.commoncontrollers.actions;

import games.commoncontrollers.HeroController;
import core.interfaces.FightStrategy;
import core.model.entity.Hero;
import core.model.entity.LivingEntity;
import core.model.entity.Monster;
import core.model.item.Item;
import core.model.item.spell.Spell;

/**
 * A type of fight strategy - casting spells
 * * @author Chris Mary Benson.
 * @version 1.0
 */

public class CastSpell implements FightStrategy {

    HeroController heroController = new HeroController();

    @Override
    public void performFightAction(LivingEntity attacker, LivingEntity target, Item item)
    {
        Hero hero = (Hero) attacker;
        //Monster monster = (Monster) target;
        Spell spell = (Spell) item;

        hero.setMana(hero.getMana() - spell.getManaCost());

        // --- DELEGATE MATH TO HERO CONTROLLER ---
        double damage = heroController.calculateSpellDamage(hero, spell);

        target.takeDamage(damage);
        System.out.println("Cast " + spell.getType() + ": " + spell.getName() + " for " + (int)damage + " damage.");

        spell.castEffect((Monster) target);
    }
}
