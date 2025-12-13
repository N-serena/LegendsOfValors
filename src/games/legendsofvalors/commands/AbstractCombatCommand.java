package games.legendsofvalors.commands;

import core.interfaces.FightStrategy;
import core.model.Party;
import games.legendsofvalors.controller.battle.LoVBattleProxy;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.model.world.LovBoard;

public abstract class AbstractCombatCommand implements LovCommand {
    protected LovBoard board;
    protected ValorHero hero;
    protected ValorMonster target;
    protected Party party;

    public AbstractCombatCommand(LovBoard board, ValorHero hero, ValorMonster target, Party party) {
        this.board = board;
        this.hero = hero;
        this.target = target;
        this.party = party;
    }

    // Abstract methods for the subclasses to implement
    protected abstract FightStrategy getStrategy();
    protected abstract String getActionVerb();

    @Override
    public boolean execute() {
        if (target == null) return false;

        System.out.println("⚔️ " + hero.getName() + " " + getActionVerb() + " " + target.getName() + "!");

        // 1. Create the Proxy
        LoVBattleProxy proxy = new LoVBattleProxy();

        // 2. Start Battle using the specific Strategy
        boolean battleSuccess = proxy.startBattle(hero, getStrategy(), party, board);

        // 3. Common Cleanup Logic
        if (target.isFainted()) {
            // Check if it's still on the board before trying to remove
            if (board.getMonsterPosition(target) != null) {
                System.out.println("   -> " + target.getName() + " has been defeated and removed.");
                board.removeMonster(target);
            }
        }
        return battleSuccess;
    }
}