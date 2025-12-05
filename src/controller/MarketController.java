package controller;

import model.entity.Hero;
import model.item.Item;
import model.market.Market;
import model.Party;
import view.Colors;

import java.util.List;
import java.util.Scanner;

/**
 * Manages the Market interactions (Buying and Selling).
 * Enforces rules about Gold and Level requirements.
 * * @author Serena N.
 * @version 1.0
 */
public class MarketController {
    private Scanner scanner;
    private InventoryController inventoryController; // Needed for safe removal

    public MarketController(Scanner scanner, InventoryController invController) {
        this.scanner = scanner;
        this.inventoryController = invController;
    }

    public void enterMarket(Market market, Party party) {
        System.out.println("\n--- Welcome to the Market! ---");
        boolean shopping = true;

        while (shopping) {
            System.out.println("\nWho is shopping? (Enter ID or 0 to exit):");
            for (int i = 0; i < party.getSize(); i++) {
                Hero h = party.getHero(i);
                System.out.printf("%d. %s (Gold: %.0f)\n", (i + 1), h.getName(), h.getGold());
            }

            if (scanner.hasNextInt()) {
                int heroIndex = scanner.nextInt();
                if (heroIndex == 0) {
                    shopping = false;
                } else if (heroIndex > 0 && heroIndex <= party.getSize()) {
                    handleShopper(party.getHero(heroIndex - 1), market);
                } else {
                    System.out.println("Invalid hero.");
                }
            } else {
                scanner.next();
            }
        }
        System.out.println("Leaving market...");
    }

    private void handleShopper(Hero hero, Market market) {
        boolean active = true;
        while (active) {
            System.out.println("\n" + hero.getName() + " is at the counter.");
            System.out.println("1. Buy \n2. Sell \n0. Back");
            System.out.print("> ");
            String choice = scanner.next();

            if (choice.equals("1")) buyLoop(hero, market);
            else if (choice.equals("2")) sellLoop(hero);
            else if (choice.equals("0")) active = false;
        }
    }

    private void buyLoop(Hero hero, Market market) {
        List<Item> wares = market.getItems();
        System.out.println("\n--- ITEMS FOR SALE ---");

        // ADD HEADER ROW
        System.out.printf(" %-3s %-20s %-8s %-5s\n", "ID", "Name", "Cost", "Lvl");
        System.out.println(" ----------------------------------------");

        for (int i = 0; i < wares.size(); i++) {
            Item it = wares.get(i);

            // Color Logic (Optional polish)
            String color = view.Colors.RESET;
            if (it.getMinLevel() >= 8) color = view.Colors.PURPLE;
            else if (it.getMinLevel() >= 4) color = view.Colors.CYAN;

            // PRINT WITH LEVEL CLEARLY VISIBLE
            System.out.printf(" %-3d %s%-20s%s %-8.0f %-5d\n",
                    (i+1),
                    color, it.getName(), view.Colors.RESET,
                    it.getPrice(),
                    it.getMinLevel() // <--- The missing info
            );
        }
        System.out.println(" ----------------------------------------");
        System.out.print("Enter Item ID to buy (0 to cancel): ");

        if (scanner.hasNextInt()) {
            int id = scanner.nextInt();
            if (id > 0 && id <= wares.size()) {
                Item item = wares.get(id - 1);

                // Validation Logic
                if (hero.getLevel() < item.getMinLevel()) {
                    System.out.println(view.Colors.RED + "Cannot Buy: You need Level " + item.getMinLevel() + "!" + view.Colors.RESET);
                } else if (hero.getGold() < item.getPrice()) {
                    System.out.println(view.Colors.RED + "Cannot Buy: Not enough Gold!" + view.Colors.RESET);
                } else {
                    hero.decreaseGold(item.getPrice());
                    hero.addItem(item);
                    System.out.println("Purchase successful: " + item.getName());
                }
            }
        } else scanner.next();
    }

    private void sellLoop(Hero hero) {
        List<Item> inv = hero.getInventory();
        if (inv.isEmpty()) { System.out.println("Inventory empty."); return; }

        for (int i = 0; i < inv.size(); i++) {
            System.out.printf("%d. %-20s (Sell: %.0f)\n", (i+1), inv.get(i).getName(), inv.get(i).getPrice()/2);
        }
        System.out.print("Enter ID to sell (0 cancel): ");

        if (scanner.hasNextInt()) {
            int id = scanner.nextInt();
            if (id > 0 && id <= inv.size()) {
                Item item = inv.get(id - 1);
                hero.addGold(item.getPrice() / 2.0);
                // Use InventoryController to safely unequip if needed
                inventoryController.removeItemSafely(hero, item);
                System.out.println("Sold " + item.getName());
            }
        } else scanner.next();
    }
}