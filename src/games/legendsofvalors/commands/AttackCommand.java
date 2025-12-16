package games.legendsofvalors.commands;

import core.interfaces.FightStrategy;
import core.model.Party;
import games.commoncontrollers.actions.Attack;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.model.world.LovBoard;

public class AttackCommand extends AbstractCombatCommand {

    public AttackCommand(LovBoard board, ValorHero hero, ValorMonster target, Party party) {
        super(board, hero, target, party);
    }

    @Override
    protected FightStrategy getStrategy() {
        return new Attack();
    }

    @Override
    protected String getActionVerb() {
        return "attacks";
    }
}