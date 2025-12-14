package games.legendsofvalors.states;

import core.model.GameDatabase;
import core.model.entity.Monster;
import games.commoncontrollers.actions.Attack; // P2 Strategy
import games.legendsofvalors.controller.LovGameController;
import games.legendsofvalors.controller.battle.LoVBattleProxy;
import games.legendsofvalors.interfaces.Battle;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.model.world.LovBoard;

import java.util.Map;
import java.util.Set;

public class MonsterTurnState implements LovGameState {

    @Override
    public void execute(LovGameController context) {

        context.displayBoard();

        System.out.println("\n=== MONSTER TURN ===");
        LovBoard board = context.getBoard();

        // 1. ATTACK PHASE
        Map<ValorMonster, Set<ValorHero>> attackers = board.getMonstersReadyToAttack(1);

        if (attackers.isEmpty()) {
            System.out.println("No monsters are in range to attack.");
        } else {
            for (Map.Entry<ValorMonster, Set<ValorHero>> entry : attackers.entrySet()) {
                ValorMonster monster = entry.getKey();
                ValorHero target = entry.getValue().iterator().next(); // Simple AI: Pick first target

                System.out.println("⚠️ " + monster.getName() + " attacks " + target.getName() + "!");

                // USE SHARED STRATEGY
//                Attack attackAI = new Attack();
//                // performFightAction(attacker, target, item) - Item is null for monsters
//                attackAI.performFightAction(monster, target, null);

                Battle monsterFight = new LoVBattleProxy();
                monsterFight.startBattle(monster, target, new Attack(), context.getParty(), board);
            }
        }

        // 2. MOVEMENT PHASE
        System.out.println("Monsters are advancing...");
        board.advanceMonsters();

        // 3. CHECK LOSS CONDITION
        for (Monster m : GameDatabase.getInstance().getAllMonsters()) {
            if (m instanceof ValorMonster) {
                LovBoard.Position pos = board.getMonsterPosition((ValorMonster) m);
                if (pos != null && pos.row == games.legendsofvalors.util.GameConfig.BOARD_SIZE - 1) {
                    System.out.println("\n***********************************");
                    System.out.println(" GAME OVER! A Monster breached the Nexus!");
                    System.out.println("***********************************");
                    System.exit(0);
                }
            }
        }

        // 4. TRANSITION
        context.setState(new RoundEndState());
    }
}