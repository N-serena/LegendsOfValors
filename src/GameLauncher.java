import java.util.Scanner;
import core.interfaces.GameEngine;
import games.legendsofvalors.controller.LovGameController;
import games.monstersandheroes.contoller.MHGameController;

public class GameLauncher {

    public void launch() {
        System.out.println("Welcome to the RPG System!");
        System.out.println("--------------------------------");
        System.out.println("1. Legends of Monsters and Heroes (Original)");
        System.out.println("2. Legends of Valor (New MOBA)");
        System.out.println("--------------------------------");
        System.out.print("Choose your game: ");

        Scanner input = new Scanner(System.in);
        String choice = input.next();
        GameEngine game = null;

        // Factory Logic
        if (choice.equals("1")) {
            game = new MHGameController();
        } else if (choice.equals("2")) {
            game = new LovGameController();
        } else {
            System.out.println("Invalid selection. Exiting.");
            return;
        }

        // Start the selected game
        game.startGame();
    }
}