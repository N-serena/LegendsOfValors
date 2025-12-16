package games.legendsofvalors.commands;

import core.interfaces.FightStrategy;
import core.model.Party;
import games.commoncontrollers.actions.CastSpell;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.model.world.LovBoard;

public class CastSpellCommand extends AbstractCombatCommand {

    public CastSpellCommand(LovBoard board, ValorHero hero, ValorMonster target, Party party) {
        super(board, hero, target, party);
    }

    @Override
    protected FightStrategy getStrategy() {
        return new CastSpell();
    }

    @Override
    protected String getActionVerb() {
        return "casts a spell on";
    }
}