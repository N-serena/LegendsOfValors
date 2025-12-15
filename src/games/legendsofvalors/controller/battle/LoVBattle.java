package games.legendsofvalors.controller.battle;

import core.model.Party;
import core.model.entity.Hero;
import core.interfaces.Board;
import games.commoncontrollers.InventoryController;
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
import java.util.Scanner;

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
    private InventoryController inventoryController;

    public LoVBattle() {
        valorHeroController = new LoVHeroController();
        partyController = new PartyController();
        inventoryController = new InventoryController(new Scanner(System.in));
    }

    /**
     * Main entry point for the battle
     * @param attacker,target,fightStrategy,party,board
     * @return isActionDone to indicate the outcome of the attack
     */
    @Override
    public boolean startBattle(LivingEntity attacker, LivingEntity target, FightStrategy fightStrategy, Party party, Board board)
    {
        //first method to be called from the game controller
        this.currentFightStrategy = fightStrategy;
        this.attacker = attacker;
        this.target = target;
        this.board = (LovBoard) board;
        this.party = party;

        addHeroObservers(party); //registering the party members as observers
        boolean isActionDone = fight();

        return isActionDone;
    }

    /**
     * To carry out the fight actions of the attacker on the target
     * @return boolean value to indicate the outcome of the attack
     */
    public boolean fight()
    {
        //have to check if there are multiple monster/heroes in range. If there are, give hero the option to choose their target
        if (this.currentFightStrategy instanceof Attack)
        {
            printAction("ATTACK");
            //if attacker is a hero
            if (attacker instanceof ValorHero) {
                Weapon w = (Weapon) selectAttackItem(attacker); //get the desired weapon of the hero

                //pass control to fightStrategy to peform attack
                currentFightStrategy.performFightAction(attacker, target, w); //transfer control to the fightstrategy to perform the attack

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
                }
            }
            //if attacker is a monster
            else if (attacker instanceof ValorMonster)
            {
                //randomly select hero target
                ValorHero target = selectHeroTarget();

                //transfer control to the specific fight strategy to carry out the attack
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
        //if the hero wants to cast a spell
        else if (this.currentFightStrategy instanceof CastSpell)
        {
            printAction("SPELL ATTACK");
            Spell s = (Spell) selectSpellItem(attacker);
            currentFightStrategy.performFightAction(attacker, target, s);
            if (target.isFainted())
            {
                //distribute rewards
                partyController.defeatedMonster(target.getLevel());
            }
            inventoryController.consumeItem((Hero) attacker, s);
        }
        return true;
    }

    /**
     * A method to register the party members as observers
     * @param party The party of heroes
     */
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

    /**
     * To get the heroes in range for the monster to attack
     * @return ValorHero the selected target
     */
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

    public void printAction(String action)
    {
        System.out.println();
        System.out.println("[ " + action + " ] ");
    }
}
