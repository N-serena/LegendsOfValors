package controller;

import model.entity.Hero;
import model.item.Item;
import model.item.Potion;
import model.item.Weapon;
import model.item.Armor;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Manages Inventory interactions (Equipping items, Using Potions).
 * Extracted to allow re-use between Roaming and Battle states.
 * * @author Serena N.
 * @version 2.0
 */
public class InventoryController {
    private Scanner scanner;

    public InventoryController(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Logic to handle equipping items safely.
     * Checks item type, updates the Hero model, and provides feedback.
     */
    public boolean openEquipMenu(Hero hero) {
        System.out.println("\n--- EQUIP MENU: " + hero.getName() + " ---");
        List<Item> inv = hero.getInventory();
        List<Item> gear = new ArrayList<>();

        // Filter inventory for equippables
        for (Item i : inv) {
            if (i instanceof Weapon || i instanceof Armor) gear.add(i);
        }

        if (gear.isEmpty()) {
            System.out.println("No equipment available.");
            return false;
        }

        for (int i = 0; i < gear.size(); i++) {
            System.out.println((i + 1) + ". " + gear.get(i).getName());
        }
        System.out.println("0. Cancel");
        System.out.print("Select item to equip: ");

        if (scanner.hasNextInt()) {
            int idx = scanner.nextInt();
            if (idx > 0 && idx <= gear.size()) {
                equipItem(hero, gear.get(idx - 1));
                return true;
            }
        } else {
            scanner.next();
        }
        return false;
    }

    /**
     * Helper to perform the actual equip logic.
     */
    public void equipItem(Hero hero, Item item) {
        if (item instanceof Weapon) {
            hero.setEquippedWeapon((Weapon) item);
            System.out.println("Equipped Weapon: " + item.getName());
        } else if (item instanceof Armor) {
            hero.setEquippedArmor((Armor) item);
            System.out.println("Equipped Armor: " + item.getName());
        }
    }

    public boolean openPotionMenu(Hero hero) {
        List<Item> inv = hero.getInventory();
        List<Potion> potions = new ArrayList<>();
        for (Item i : inv) {
            if (i instanceof Potion) potions.add((Potion) i);
        }

        if (potions.isEmpty()) {
            System.out.println("No potions available.");
            return false;
        }

        for (int i = 0; i < potions.size(); i++) {
            System.out.println((i + 1) + ". " + potions.get(i).getName());
        }
        System.out.print("Select potion (0 to cancel): ");

        if (scanner.hasNextInt()) {
            int idx = scanner.nextInt();
            if (idx > 0 && idx <= potions.size()) {
                Potion p = potions.get(idx - 1);
                // Apply stats
                hero.setHp(hero.getHp() + p.getAttributeIncrease());
                hero.setMana(hero.getMana() + p.getAttributeIncrease());
                // Remove consumed item
                hero.removeItemFromList(p);
                System.out.println(hero.getName() + " drank " + p.getName());
                return true;
            }
        } else {
            scanner.next();
        }
        return false;
    }

    /**
     * Safe removal of items (Selling/Dropping).
     * Checks if item is currently equipped and unequips it first.
     */
    public void removeItemSafely(Hero hero, Item item) {
        if (hero.getEquippedWeapon() == item) {
            hero.setEquippedWeapon(null);
            System.out.println("(Auto-unequipped " + item.getName() + ")");
        }
        if (hero.getEquippedArmor() == item) {
            hero.setEquippedArmor(null);
            System.out.println("(Auto-unequipped " + item.getName() + ")");
        }
        hero.removeItemFromList(item);
    }

    /**
     * Consumes a single-use item (Potion or Spell).
     */
    public void consumeItem(model.entity.Hero hero, model.item.Item item) {
        // We call the Model's atomic remover
        hero.removeItemFromList(item);
    }
}