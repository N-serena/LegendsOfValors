package games.legendsofvalors.interfaces;

import core.interfaces.FightStrategy;
import core.model.Party;
import core.model.entity.LivingEntity;
import core.interfaces.Board;

public interface Battle {
    public boolean startBattle(LivingEntity attacker, LivingEntity target, FightStrategy fightStrategy, Party party, Board board);
}
