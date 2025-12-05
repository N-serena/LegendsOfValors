package controller;
import java.util.Scanner;

public class InputHandler {
    private Scanner scanner;

    public InputHandler() {
        this.scanner = new Scanner(System.in);
    }

    public String getInput() {
        System.out.print("> ");
        return scanner.next();
    }
}