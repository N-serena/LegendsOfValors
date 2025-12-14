package games.legendsofvalors.controller.battle;

import core.model.Party;
import core.model.entity.Hero;
import core.interfaces.Board;
import games.commoncontrollers.PartyController;
import games.commoncontrollers.actions.Attack;
import games.commoncontrollers.actions.CastSpell;
import core.interfaces.FightStrategy;
import games.commoncontrollers.BattleController;
import games.legendsofvalors.controller.LoVHeroController;
import games.legendsofvalors.interfaces.Battle;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import core.model.entity.LivingEntity;
import core.model.item.spell.Spell;
import core.model.item.Weapon;
import games.legendsofvalors.model.world.LovBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles the Turn-Based Combat State.
 * delegates Inventory actions to InventoryController.
 * * @author Chris Mary Benson.
 * @version 1.0 (Refactored)
 */

public class LoVBattle extends BattleController implements Battle {

    private LivingEntity attacker;
    private LivingEntity target;
    private LovBoard board;
    private PartyController partyController;

    private LoVHeroController valorHeroController;

    public LoVBattle() {
        valorHeroController = new LoVHeroController();
        partyController = new PartyController();
    }

    @Override
    public boolean startBattle(LivingEntity attacker, LivingEntity target, FightStrategy fightStrategy, Party party, Board board)
    {
        //first method to be called from the game controller
        this.currentFightStrategy = fightStrategy;
        this.attacker = attacker;
        this.target = target;
        this.board = (LovBoard) board;
        this.party = party;

        addHeroObservers(party);
        boolean isActionDone = fight();

        return isActionDone;
    }

    public boolean fight()
    {
        //have to check if there are multiple monster/heroes in range. If there are, give hero the option to choose their target
        if (this.currentFightStrategy instanceof Attack)
        {
            if (attacker instanceof ValorHero) { //maybe a battleProxyclass to check if hero has the weapons and spells before creating battle
                Weapon w = (Weapon) selectAttackItem(attacker);

                //pass control to fightStrategy to peform attack
                currentFightStrategy.performFightAction(attacker, target, w);

                //check if monster has fainted
                if (target.isFainted())
                {
                    System.out.println("...");
                    System.out.println(target.getName() + " has fainted.");
                    System.out.println(attacker.getName() + " has defeated " + target.getName() + " !");
                    System.out.println();

                    //logic for removing monster
                    board.removeMonster((ValorMonster) target); //internally handles the logic for removing monster from the board

                    //rewards for the party when a monster is defeated
                    partyController.defeatedMonster(target.getLevel());
                    //distributeRewards(party);
                }
            }
            else if (attacker instanceof ValorMonster)
            {
                //randomly select hero target
                //check if terrain bonuses are applied to the monster and then do the attack
                ValorHero target = selectHeroTarget();

                currentFightStrategy.performFightAction(attacker, target, null);

                if (target.isFainted())
                {
                    System.out.println("...");
                    System.out.println(target.getName() + " has fainted");

                    System.out.println("...");
                    System.out.println(target.getName() + " has fainted.");
                    System.out.println(attacker.getName() + " has defeated " + target.getName() + " !");
                    System.out.println();

                    //hero respawns at their home nexus
                    valorHeroController.respawnHero(target, board);

                    System.out.println(target.getName() + " has respawned at their home nexus.");
                    System.out.println();
                }

            }
        }
        else if (this.currentFightStrategy instanceof CastSpell)
        {
            Spell s = (Spell) selectSpellItem(attacker);
            currentFightStrategy.performFightAction(attacker, target, s);
        }
        return true;
    }

    public void addHeroObservers(Party party)
    {
        for (Hero hero : party.getHeroes())
        {
            if (!hero.isFainted())
            { partyController.addObserver((ValorHero) hero);}
        }
    }

//    public ValorMonster selectMonsterTarget()
//    {
//        int i = 0;
//        int choice;
//
//        List<ValorMonster> monsters = new ArrayList<>(board.getMonstersInRange((ValorHero) attacker, 4));
//
//        //only one monster
//        if (monsters.size() == 1)
//        {
//            return monsters.get(0);
//        }
//        //many monsters in range, give choice to user to select the target
//        else {
//            for (ValorMonster monster : monsters) {
//                System.out.println("[" + i + "] " + monster);
//                System.out.println();
//                System.out.println("Choose your target !");
//
//                choice = inputHandler.getIntegerInput(1, monsters.size());
//
//                return monsters.get(choice - 1);
//            }
//        }
//        return null;
//    }

    public ValorHero selectHeroTarget()
    {
        Random rand = new Random();

        List<ValorHero> heroes = new ArrayList<>(board.getHeroesInRange((ValorMonster) attacker, 4));

        //only one monster
        if (heroes.size() == 1)
        {
            return heroes.get(0);
        }
        else {
            int index = rand.nextInt(heroes.size());
            return heroes.get(index);
        }
    }
}
