package games.commoncontrollers;
import java.util.Scanner;

/**
 * A centralized class consisting of methods to handle integer and string inputs
 * * @author Chris Mary Benson
 * @version 1.0
 */

public class InputHandler {
    private Scanner scanner;

    public InputHandler() {
        this.scanner = new Scanner(System.in);
    }

    public String getInput() {
        System.out.print("> ");
        return scanner.next().toUpperCase();
    }

    public void enter()
    {
        System.out.println();
        System.out.println("Press Enter to continue...");
        if (scanner.hasNextLine()) scanner.nextLine();
    }


    /**
     * A method to get input of the player meant for integer operations and checking if the input is valid
     * @param min,max the range of values required
     * @return string input
     */
    public String getIntInput(int min, int max) {

        while (true)
        {
            System.out.print("<> ");
            String input = scanner.nextLine().trim();
            input = input.toUpperCase();

            if (input.equals("Q"))//can be quit
            {
                return input;
            }

            if (!input.matches("\\d+")) //checking if it is a digit
            {
                System.out.println("Invalid input! Please enter a number.");
                continue;
            }

            int value = Integer.parseInt(input);

            if (value < min || value > max) //checkng if it falls within the range of values required
            {
                System.out.println("Please enter a valid index!");
                continue;
            }

            return input;
        }
    }

    /**
     * A method to get input of the player meant for integer operations and checking if the input is valid
     * @param min,max the range of values required
     * @return string input
     */
    public int getIntegerInput(int min, int max) {

        int input;

        while (true)
        {
            System.out.print("<> ");

            if (scanner.hasNextInt()) {
                input = scanner.nextInt();
                scanner.nextLine();

                if (input < min || input > max) //checkng if it falls within the range of values required
                {
                    System.out.println("Please enter a valid index!");
                    continue;
                }
                else {
                    break;
                }
            }
        }

        return input;
    }
}