package games.commoncontrollers;

import core.model.entity.Hero;
import core.model.item.*;

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
    public List<Spell> spells;
    public List<Weapon> weapons;

    public InventoryController(Scanner scanner) {
        this.scanner = scanner;
        this.spells = new ArrayList<>();
        this.weapons = new ArrayList<>();
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
            if (hero.getHandsInUse() == 2 || hero.getHandsInUse() + ((Weapon) item).getRequiredHands() > 2) {

                System.out.println("Cannot equip any more weapons!");
            }
            else {
                hero.setEquippedWeapon((Weapon) item);
                hero.setHandsInUse(((Weapon) item).getRequiredHands());
                System.out.println("Equipped Weapon: " + item.getName());
            }
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

    public boolean openSpellsMenu(Hero hero)
    {
        spells.clear();

        for (Item i : hero.getInventory()) if (i instanceof Spell) spells.add((Spell) i);

        if (spells.isEmpty()) { System.out.println("No spells."); return false; }

        for (int i = 0; i < spells.size(); i++) {
            System.out.printf("%d. %s (Mana: %.0f)\n", (i+1), spells.get(i).getName(), spells.get(i).getManaCost());
        }

        return true;
    }

    public boolean checkForSpells(Hero hero)
    {
        spells.clear();

        for (Item i : hero.getInventory()) if (i instanceof Spell) spells.add((Spell) i);

        if (spells.isEmpty()) { System.out.println("No spells."); return false; }

        return true;
    }

    public boolean openEquippedWeaponsMenu(Hero hero)
    {
        weapons.clear();

        for (Item i : hero.getEquippedWeapon()) if (i instanceof Weapon) weapons.add((Weapon) i);

        if (weapons.isEmpty()) { System.out.println("No Weapons."); return false; }

        for (int i = 0; i < weapons.size(); i++) {
            System.out.printf("%d. %s (Damage: %.0f)\n", (i+1), weapons.get(i).getName(), weapons.get(i).getDamage());
        }

        return true;
    }

    public boolean checkForEquippedWeapons(Hero hero)
    {
        weapons.clear();

        for (Item i : hero.getEquippedWeapon()) if (i instanceof Weapon) weapons.add((Weapon) i);

        if (weapons.isEmpty()) { System.out.println("No Weapons."); return false; }

        return true;
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
    public void consumeItem(Hero hero, Item item) {
        // We call the Model's atomic remover
        hero.removeItemFromList(item);
    }
}