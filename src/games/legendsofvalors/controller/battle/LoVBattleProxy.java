package games.legendsofvalors.controller.battle;

import core.interfaces.FightStrategy;
import core.model.Party;
import core.model.entity.Hero;
import core.model.entity.LivingEntity;
import core.interfaces.Board;
import games.commoncontrollers.InventoryController;
import games.commoncontrollers.actions.Attack;
import games.commoncontrollers.actions.CastSpell;
import games.legendsofvalors.interfaces.Battle;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.model.world.LovBoard;

import java.util.Scanner;
import java.util.Set;

/**
 * Battle Proxy class for Legends of Valor, to do additional housekeeping tasks. Implements the Battle interface
 */
public class LoVBattleProxy implements Battle {

    private LoVBattle battle;
    private InventoryController inventoryController;
    Scanner scanner;

    public LoVBattleProxy() {
        this.scanner = new Scanner(System.in);
        inventoryController = new InventoryController(scanner);
    }

    /**
     * main point to get to in the battleproxy. Validity checks are done here
     */
    @Override
    public boolean startBattle(LivingEntity attacker, LivingEntity target, FightStrategy fightStrategy, Party party, Board board)
    {
        boolean canBattleStart = false;

        if (attacker instanceof ValorHero) {
            if (fightStrategy instanceof Attack) {
                //checking if heroes have equipped weapons in their inventory
                if (!inventoryController.checkForEquippedWeapons((Hero) attacker)) {
                    System.out.println("Attack not possible.");
                    return false; //return to main game loop
                }
                //if there are any monsters in range
                if(!checkIfMonstersInRange(attacker, (LovBoard) board)) { return false; };
            }
            else if (fightStrategy instanceof CastSpell) {
                //checking if the hero has any spells in thei ivnentory
                if (!inventoryController.checkForSpells((Hero) attacker)) {
                    System.out.println("Casting spells not possible.");
                    return false; //return to main game loop
                }
            }
        }
        else if (attacker instanceof ValorMonster) {
            //checking if there are any heroes in the monster's attack range
            if(!checkIfHeroesInRange(attacker, (LovBoard) board)) { return false; }
        }

        if (battle == null)
        {
            //initializing battle object only if all the checks are successfull
            battle = new LoVBattle();
        }

        //start the real battle
        return battle.startBattle(attacker, target, fightStrategy, party, board);
    }

    /**
     * To check if there are monsters in range (when hero is the attacker)
     */
    public boolean checkIfMonstersInRange(LivingEntity attacker, LovBoard board)
    {
        Set<ValorMonster> monsters = board.getMonstersInRange((ValorHero) attacker, 4);

        if (monsters.isEmpty())
        {
            System.out.println("Attack not possible! No monsters in range.");
            return false;
        }
        return true;
    }

    /**
     * To check if there are heroes in range (when monster is the attacker)
     */
    public boolean checkIfHeroesInRange(LivingEntity attacker, LovBoard board)
    {
        Set<ValorHero> heroes = board.getHeroesInRange((ValorMonster) attacker, 1);

        if (heroes.isEmpty())
        {
            return false;
        }
        return true;
    }
}