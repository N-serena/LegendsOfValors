package games.legendsofvalors.commands;

public interface LovCommand {
    /**
     * Executes the command logic.
     * @return true if the action was successful, false if it was invalid (blocked, illegal move, etc).
     */
    boolean execute();
}