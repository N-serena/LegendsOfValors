package games.legendsofvalors.commands;

import core.interfaces.FightStrategy;
import core.model.Party;
import games.commoncontrollers.actions.Attack;
import games.legendsofvalors.controller.battle.LoVBattleProxy;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.model.world.LovBoard;

public class AttackCommand implements LovCommand {
    private LovBoard board;
    private ValorHero hero;
    private ValorMonster target;
    private Party party; // Required by P2's BattleProxy

    public AttackCommand(LovBoard board, ValorHero hero, ValorMonster target, Party party) {
        this.board = board;
        this.hero = hero;
        this.target = target;
        this.party = party;
    }

    @Override
    public boolean execute() {
        if (target == null) return false;

        System.out.println("⚔️ " + hero.getName() + " initiates attack on " + target.getName() + "!");

        // 1. Define Strategy (Attack vs Spell)
        FightStrategy attackStrategy = new Attack();

        // 2. Create Proxy
        // calculating damage, checking equipped weapons, checking range
        LoVBattleProxy proxy = new LoVBattleProxy();

        // 3. Start Battle
        // We pass the party because system needs it for observers/rewards
        boolean battleSuccess = proxy.startBattle(hero, attackStrategy, party, board);

        // 4. Cleanup Logic After Battle
        if (target.isFainted()) {
            if (board.getMonsterPosition(target) != null) {
                System.out.println("   -> " + target.getName() + " has been defeated and removed.");
                board.removeMonster(target);
            }
        }

        return battleSuccess;
    }
}