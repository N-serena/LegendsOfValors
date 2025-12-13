package games.legendsofvalors.states;

import core.model.GameDatabase;
import games.legendsofvalors.controller.LovGameController;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.model.factory.MonsterFactory;
import games.legendsofvalors.model.world.LovBoard;
import games.legendsofvalors.states.HeroTurnState;
import games.legendsofvalors.states.LovGameState;

import java.util.List;

public class RoundEndState implements LovGameState {

    // Configurable Spawn Interval (Default 8)
    private static final int SPAWN_INTERVAL = 8;

    @Override
    public void execute(LovGameController context) {
        System.out.println("\n=== ROUND " + context.getRoundNumber() + " ENDED ===");

        // 1. Regenerate Heroes (10% HP/MP)
        performRegen(context.getHeroes());

        // 2. Check Spawn Condition
        if (context.getRoundNumber() % SPAWN_INTERVAL == 0) {
            spawnNewWave(context);
        }

        // 3. Increment Round and Loop
        context.incrementRound();
        context.setState(new HeroTurnState());
    }

    private void spawnNewWave(LovGameController context) {
        System.out.println("⚠️ A NEW WAVE OF MONSTERS IS APPROACHING! ⚠️");

        //Determine Difficulty (Highest Hero Level)
        int maxLevel = 1;
        for (ValorHero h : context.getHeroes()) {
            if (h.getLevel() > maxLevel) maxLevel = h.getLevel();
        }

        //Use FACTORY to create the wave
        //use singleton list of all monsters from GameDatabase
        List<ValorMonster> newWave = MonsterFactory.createWave(maxLevel, GameDatabase.getInstance().getAllMonsters());

        // Place on Board
        LovBoard board = context.getBoard();
        for (ValorMonster m : newWave) {
            // Find the specific column for this lane (0, 3, or 6)
            int col = getSpawnColumn(m.getLane());

            // Place at Row 0 (Monster Nexus)
            // If Row 0 is blocked, the Factory logic implies we might overwrite or stack?
            if (!board.placeMonster(m, 0, col)) {
                System.out.println("Lane " + m.getLane() + " is full! Reinforcements delayed.");
            } else {
                System.out.println(m.getName() + " spawned in " + m.getLane() + " lane.");
            }
        }
    }

    private void performRegen(List<ValorHero> heroes) {
        for (ValorHero h : heroes) {
            if (!h.isFainted()) {
                // Regen logic
                h.setHp(h.getHp() * 1.1);
                h.setMana(h.getMana() * 1.1);
            }
        }
        System.out.println("Heroes rested and recovered HP/Mana.");
    }

    private int getSpawnColumn(String lane) {
        switch (lane) {
            case "Top": return 0; // or 1
            case "Mid": return 3; // or 4
            case "Bot": return 6; // or 7
            default: return 0;
        }
    }
}