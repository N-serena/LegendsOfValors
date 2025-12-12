package games.commoncontrollers;

import core.interfaces.FightStrategy;
import core.model.Party;
import core.model.entity.Hero;
import core.model.entity.LivingEntity;
import core.model.item.Item;
import core.model.item.Spell;
import core.model.item.Weapon;

import java.util.Scanner;

/**
 * An abstract class for battle
 * * @author Chris Mary Benson.
 * @version 1.0
 */

public abstract class BattleController {

    protected Party party;
    protected FightStrategy currentFightStrategy;

    protected HeroController heroController;
    protected InventoryController inventoryController;
    protected InputHandler inputHandler;
    protected Scanner scanner;

    public BattleController() {
        inventoryController = new InventoryController(scanner);
        this.inputHandler = new InputHandler();
    }

    public Item selectAttackItem(LivingEntity attacker) {
        Weapon w = null;

        if (inventoryController.openEquippedWeaponsMenu((Hero) attacker)) {
            System.out.print("Select Weapon (0 cancel): "); //selecting item

            int idx = inputHandler.getIntegerInput(1,inventoryController.weapons.size());

            w = inventoryController.weapons.get(idx - 1);
        }
        else{
            return null;
        }
        return w;
    }

    public Item selectSpellItem(LivingEntity attacker) {
        Spell s = null;

        if (inventoryController.openSpellsMenu((Hero) attacker)) {
            System.out.print("Select Spell (0 cancel): ");

            int idx = inputHandler.getIntegerInput(1,inventoryController.spells.size());

            s = inventoryController.spells.get(idx - 1);

            // 2. Check Requirements (Model Query)
            if (((Hero) attacker).getMana() < s.getManaCost()) {
                System.out.println("Not enough Mana.");
                return null;
            }
        }
        else {
            return null;
        }
        return s;
    }
}
