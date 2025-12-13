package games.legendsofvalors.states;

import core.model.GameDatabase;
import core.model.entity.Monster;
import games.legendsofvalors.controller.LovCombat;
import games.legendsofvalors.controller.LovGameController;
import games.legendsofvalors.model.ValorHero;
import games.legendsofvalors.model.ValorMonster;
import games.legendsofvalors.model.world.LovBoard;
import games.legendsofvalors.states.LovGameState;
import games.legendsofvalors.states.RoundEndState;

import java.util.Map;
import java.util.Set;

public class MonsterTurnState implements LovGameState {

    @Override
    public void execute(LovGameController context) {
        System.out.println("\n=== MONSTER TURN ===");
        LovBoard board = context.getBoard();
        LovCombat combat = new LovCombat(board);

        //ATTACK PHASE
        // Ask the board: "Who can attack?" (Range 1 includes diagonals)
        Map<ValorMonster, Set<ValorHero>> attackers = board.getMonstersReadyToAttack(1);

        if (attackers.isEmpty()) {
            System.out.println("No monsters are in range to attack.");
        } else {
            for (Map.Entry<ValorMonster, Set<ValorHero>> entry : attackers.entrySet()) {
                ValorMonster monster = entry.getKey();
                // If multiple heroes are in range, pick the first one (Simple AI)
                ValorHero target = entry.getValue().iterator().next();

                System.out.println("⚠️ " + monster.getName() + " attacks " + target.getName() + "!");

                // Use P2's Combat Logic
                // Note: We need a 'calculateMonsterDamage' in LovCombat,
                // or we use standard logic here if P2 hasn't made it yet.
                // Assuming standard logic for now:
                double dmg = monster.getBaseDamage() * 0.05; // Standard formula
                double defense = (target.getEquippedArmor() != null) ? target.getEquippedArmor().getDamageReduction() : 0;
                double actualDmg = Math.max(0, dmg - (defense * 0.05));

                target.takeDamage(actualDmg);
                System.out.println("   -> Dealt " + (int)actualDmg + " damage.");

                if (target.isFainted()) {
                    System.out.println(target.getName() + " has fainted!");
                    // Ensure Hero is removed from board or marked inactive
                    // (Regen/Respawn logic in RoundEndState will handle the rest)
                }
            }
        }

        // 2. MOVEMENT PHASE
        // The board logic automatically skips monsters that just attacked or are blocked
        System.out.println("Monsters are advancing...");
        board.advanceMonsters();

        // 3. CHECK LOSS CONDITION
        // Scan all monsters to see if any reached the Hero Nexus (Row 7)
        // (We iterate the board's monster list logic or check positions)
        // Since we don't have a direct list of all positions easily, we can iterate the active monsters.
        // Or simpler: Ask the board "Is Row 7 occupied by a monster?"

        // Let's check positions manually using the method we know exists
        // (Assuming we can get the monster list from GameDatabase or Context)
        // But better: Iterate the map in LovBoard if possible.
        // For now, let's rely on the fact that 'advanceMonsters' would have moved them.

        // CRITICAL: We need to know if we lost.
        // Let's add a helper to LovBoard or iterate known monsters.
        // For safety, let's ask the Context for the monster list (from DB) and check positions.

        for (Monster m : GameDatabase.getInstance().getAllMonsters()) {
            if (m instanceof ValorMonster) {
                LovBoard.Position pos = board.getMonsterPosition((ValorMonster) m);
                if (pos != null && pos.row == LovBoard.BOARD_SIZE - 1) {
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