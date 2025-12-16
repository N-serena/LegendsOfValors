package games.legendsofvalors.interfaces;

import games.legendsofvalors.controller.LovGameController;

public interface LovGameState {
    /**
     * Executes the logic for this state.
     * @param context The main controller, allowing states to change the currentState.
     */
    void execute(LovGameController context);
}