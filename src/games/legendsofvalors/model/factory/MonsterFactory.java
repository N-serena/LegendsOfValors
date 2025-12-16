package games.legendsofvalors.model.factory;

import core.model.entity.Monster;
import games.legendsofvalors.model.ValorMonster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MonsterFactory {

    private static final Random rand = new Random();

    /**
     * Factory Method: Creates a full wave of 3 monsters (Top, Mid, Bot).
     * * @param targetLevel The level the monsters should be scaled to (Highest Hero Level).
     * @param templates The master list of all Dragon/Spirit/Exoskeleton templates from config files.
     * @return A list of 3 ready-to-fight ValorMonsters.
     */
    public static List<ValorMonster> createWave(int targetLevel, List<Monster> templates) {
        List<ValorMonster> wave = new ArrayList<>();

        // We need one for each lane
        String[] lanes = {"Top", "Mid", "Bot"};

        for (String lane : lanes) {
            ValorMonster m = createSingleMonster(targetLevel, templates, lane);
            wave.add(m);
        }

        return wave;
    }

    /**
     * Internal helper to build a single scaled monster.
     */
    private static ValorMonster createSingleMonster(int level, List<Monster> templates, String lane) {
        // 1. Pick a random template (e.g., "Smaug" or "Blinky")
        Monster template = templates.get(rand.nextInt(templates.size()));

        // 2. Clone it into a ValorMonster (using the Copy Constructor we made earlier)
        ValorMonster newMonster = new ValorMonster(template, lane);

        // 3. Scale Stats (The Factory handles the complexity of "Balancing")
        newMonster.scaleStats(level);

        return newMonster;
    }
}