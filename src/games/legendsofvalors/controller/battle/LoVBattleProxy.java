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
 * A proxy class for battle, to do additional housekeeping tasks:- checking if the hero has any weapons or spells with them.
 * also to check if there are any targets in range to perform fight actions on
 * * @author Chris Mary Benson.
 * @version 1.0
 */

public class LoVBattleProxy implements Battle {

    private LoVBattle battle;
    private InventoryController inventoryController;
    Scanner scanner;

    public LoVBattleProxy() {
        this.scanner = new Scanner(System.in);
        inventoryController = new InventoryController(scanner);
    }

    @Override
    public boolean startBattle(LivingEntity attacker, LivingEntity target, FightStrategy fightStrategy, Party party, Board board)
    {
        boolean canBattleStart = false;

        if (attacker instanceof ValorHero) {
            if (fightStrategy instanceof Attack) {
                if (!inventoryController.checkForEquippedWeapons((Hero) attacker)) {
                    System.out.println("Attack not possible.");
                    return false; //return to main game loop
                }
                if(!checkIfMonstersInRange(attacker, (LovBoard) board)) { return false; };
            }
            else if (fightStrategy instanceof CastSpell) {
                if (!inventoryController.checkForSpells((Hero) attacker)) {
                    System.out.println("Casting spells not possible.");
                    return false; //return to main game loop
                }
            }
        }
        else if (attacker instanceof ValorMonster) {
            if(!checkIfHeroesInRange(attacker, (LovBoard) board)) { return false; }
        }

        if (battle == null)
        {
            battle = new LoVBattle();
        }

        return battle.startBattle(attacker, target, fightStrategy, party, board);
    }

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