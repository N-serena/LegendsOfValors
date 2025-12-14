import java.util.Scanner;
import core.interfaces.GameEngine;
import core.util.SoundPlayer;
import games.legendsofvalors.controller.LovGameController;
import games.monstersandheroes.contoller.MHGameController;

public class GameLauncher {

    public void launch() {
        SoundPlayer.playBackgroundMusic("soundfiles/themes.wav");

        // Clear screen (simulated)
        System.out.print("\033[H\033[2J");
        System.out.flush();

        // 1. Beautiful ASCII Title
        System.out.println(core.util.Colors.CYAN);
        System.out.println("  _    _   ______   _____     ____    ______   _____  ");
        System.out.println(" | |  | | |  ____| |  __ \\   / __ \\  |  ____| |  __ \\ ");
        System.out.println(" | |__| | | |__    | |__) | | |  | | | |__    | |__) |");
        System.out.println(" |  __  | |  __|   |  _  /  | |  | | |  __|   |  _  / ");
        System.out.println(" | |  | | | |____  | | \\ \\  | |__| | | |____  | | \\ \\ ");
        System.out.println(" |_|  |_| |______| |_|  \\_\\  \\____/  |______| |_|  \\_\\");
        System.out.println(core.util.Colors.RESET);

        System.out.println("       Welcome to the SWEN-RPG System Collection       ");
        System.out.println("=======================================================");
        System.out.println("  [1] Legends of Monsters and Heroes (The Original)");
        System.out.println("  [2] Legends of Valor (The MOBA Strategy)");
        System.out.println("=======================================================");
        System.out.print("\nSelect your destiny (1-2): ");

        Scanner input = new Scanner(System.in);
        String choice = input.next();
        GameEngine game = null;

        // 2. Factory Logic
        if (choice.equals("1")) {
            game = new MHGameController();
        } else if (choice.equals("2")) {
            game = new LovGameController();
        } else {
            System.out.println("Invalid selection. Exiting.");
            return;
        }

        // 3. Start the selected game
        game.startGame();
    }
}