package games.legendsofvalors.commands;

import core.interfaces.FightStrategy;
import core.model.Party;
import games.commoncontrollers.actions.CastSpell; // <--- The Spell Strategy
import games.legendsofvalors.controller.battle.LoVBattleProxy;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.model.world.LovBoard;

public class CastSpellCommand implements LovCommand {
    private LovBoard board;
    private ValorHero hero;
    private ValorMonster target;
    private Party party;

    public CastSpellCommand(LovBoard board, ValorHero hero, ValorMonster target, Party party) {
        this.board = board;
        this.hero = hero;
        this.target = target;
        this.party = party;
    }

    @Override
    public boolean execute() {
        if (target == null) return false;

        System.out.println("✨ " + hero.getName() + " prepares a spell against " + target.getName() + "!");

        // 1. Define Strategy (CAST SPELL)
        FightStrategy spellStrategy = new CastSpell(); // <--- Swapped Strategy

        // 2. Create Proxy
        // This handles checking for Mana, Spells in inventory, etc.
        LoVBattleProxy proxy = new LoVBattleProxy();

        // 3. Start Battle
        boolean battleSuccess = proxy.startBattle(hero, spellStrategy, party, board);

        // 4. Cleanup Logic
        if (target.isFainted()) {
            if (board.getMonsterPosition(target) != null) {
                System.out.println("   -> " + target.getName() + " has been defeated by magic!");
                board.removeMonster(target);
            }
        }

        return battleSuccess;
    }
}