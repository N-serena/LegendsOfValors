package view;

import model.Party;
import model.world.Board;
import model.world.Tile;

/** * GameView handles all console output for the RPG game.
 * It displays the title screen, intro story, game rules,
 * the game board, and various messages to the player.
 ** @author Serena N
 * @version 1.0
 */
public class GameView {

    public void printTitleScreen() {
        System.out.println(Colors.YELLOW);
        System.out.println("  _    _   ______   _____     ____    ______   _____  ");
        System.out.println(" | |  | | |  ____| |  __ \\   / __ \\  |  ____| |  __ \\ ");
        System.out.println(" | |__| | | |__    | |__) | | |  | | | |__    | |__) |");
        System.out.println(" |  __  | |  __|   |  _  /  | |  | | |  __|   |  _  / ");
        System.out.println(" | |  | | | |____  | | \\ \\  | |__| | | |____  | | \\ \\ ");
        System.out.println(" |_|  |_| |______| |_|  \\_\\  \\____/  |______| |_|  \\_\\");
        System.out.println(Colors.RESET);
        System.out.println(Colors.CYAN + "       LEGENDS: MONSTERS AND HEROES       " + Colors.RESET);
        System.out.println("          A Java Strategy RPG Adventure           ");
        System.out.println("\n--------------------------------------------------");
    }

    public void printIntroStory() {
        System.out.println("\nTHE LORE:");
        System.out.println("The world has been overrun by Spirits, Dragons, and Exoskeletons.");
        System.out.println("You must assemble a party of legendary heroes to reclaim the land.");
        System.out.println("Travel from town to town, upgrade your gear, and defeat the");
        System.out.println("monsters to reach the ultimate power (Level 10).");
        System.out.println("--------------------------------------------------");
    }

    public void printRules() {
        System.out.println("\n================ GAME RULES & CONTROLS ================");
        System.out.println(Colors.YELLOW + "GOAL:" + Colors.RESET);
        System.out.println("  - Defeat monsters to gain Experience and Gold.");
        System.out.println("  - Reach LEVEL 10 to win the game.");

        System.out.println(Colors.YELLOW + "\nMAP:" + Colors.RESET);
        System.out.println("  - " + Colors.CYAN + "H" + Colors.RESET + ": Your Party");
        System.out.println("  - " + Colors.YELLOW + "M" + Colors.RESET + ": Market (Buy Weapons, Armor, Potions, Spells)");
        System.out.println("  - " + Colors.RED + "X" + Colors.RESET + ": Inaccessible Wall");
        System.out.println("  - . : Common Terrain (Random Battles occur here!)");

        System.out.println(Colors.YELLOW + "\nCOMBAT:" + Colors.RESET);
        System.out.println("  - Turn-based: Heroes move first, then Monsters.");
        System.out.println("  - Monsters scale to match your level.");
        System.out.println("  - If all heroes faint, the game is over.");

        System.out.println(Colors.YELLOW + "\nCONTROLS:" + Colors.RESET);
        System.out.println("  - [W/A/S/D]: Move");
        System.out.println("  - [M]: Enter Market (On 'M' tile)");
        System.out.println("  - [I]: Inventory / Equip / Status");
        System.out.println("  - [Q]: Quit Game");
        System.out.println("=======================================================");
    }

    public void printBoard(Board board, Party party) {
        System.out.println("---------------------------------");
        for (int i = 0; i < board.getHeight(); i++) {
            for (int j = 0; j < board.getWidth(); j++) {
                Tile tile = board.getTile(j, i);
                String symbol = "   "; // Default space

                // Check if Party is here
                if (party.getX() == j && party.getY() == i) {
                    symbol = Colors.CYAN + " H " + Colors.RESET;
                }
                // Else print tile type
                else if (tile.getType().equals("Inaccessible")) {
                    symbol = Colors.RED + " X " + Colors.RESET;
                }
                else if (tile.getType().equals("Market")) {
                    symbol = Colors.YELLOW + " M " + Colors.RESET;
                }
                else {
                    symbol = " . "; // Common tile
                }

                System.out.print("|" + symbol);
            }
            System.out.println("|");
            System.out.println("---------------------------------");
        }
    }

    public void printMessage(String msg) {
        System.out.println(msg);
    }
}