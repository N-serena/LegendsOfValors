package games.commoncontrollers;

import core.model.GameDatabase;
import core.model.Party;
import core.model.entity.Hero;
import core.model.entity.Monster;
import core.model.item.Item;
import core.model.item.Weapon;
import core.model.item.spell.Spell;
import core.util.GameDataParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameController {

    protected List<Hero> allHeroes;
    protected List<Item> allItems;
    protected List<Monster> allMonsters;
    protected Party party;

    Scanner scanner;
    protected InventoryController inventoryController;

    public GameController() {
        allHeroes = new ArrayList<>();
        allItems = new ArrayList<>();
        allMonsters = new ArrayList<>();
        party = new Party();
        inventoryController = new InventoryController(scanner);
    }

    protected void loadGameData() throws IOException {
        //System.out.println("Loading game assets...");
        // Delegate to Singleton
        GameDatabase db = GameDatabase.getInstance();
        db.loadData();

        // Reference the loaded lists
        this.allHeroes = db.getAllHeroes();
        this.allMonsters = db.getAllMonsters();
        this.allItems = db.getAllItems();
    }

    protected void selectParty() {
        System.out.println("\n--- HERO SELECTION ---");
        int count = 0;
        while (count < 1 || count > 3) {
            System.out.print("Enter party size (1-3): ");
            if (scanner.hasNextInt()) count = scanner.nextInt();
            else scanner.next();
        }

        // Display Options
        System.out.printf("%-4s %-20s %-10s\n", "ID", "Name", "Type");
        for (int i = 0; i < allHeroes.size(); i++) {
            System.out.printf("%-4d %-20s %-10s\n", (i+1), allHeroes.get(i).getName(), allHeroes.get(i).getClass().getSimpleName());
        }

        for (int i = 1; i <= count; i++) {
            int choice = -1;
            while (choice < 1 || choice > allHeroes.size()) {
                System.out.print("Select Hero " + i + ": ");
                if (scanner.hasNextInt()) choice = scanner.nextInt();
                else scanner.next();
            }
            party.addHero(allHeroes.get(choice - 1));
        }
        System.out.println("Party assembled!");
    }

    public void getComplimentaryWeapons()
    {
        int i = 0;
        List<Item> weapons = createComplimentaryWeapons(); //creating the complimentary weapons by retrieving from the file

        for (Hero hero : party.getHeroes()) //giving each hero in the party a complimentary weapon
        {
            Item newWeapon = weapons.get(i);

            hero.getInventory().add(newWeapon);

            //hero equips complimentary weapon
            inventoryController.equipItem(hero, (Weapon) newWeapon);

            i++;
        }
    }

    public List<Item> createComplimentaryWeapons()
    {
        List<Item> initialWeapons = new ArrayList<>();
        try {
            initialWeapons = GameDataParser.parseWeapons("data_files/Initial.txt");
        }
        catch(IOException IO)
        {
            IO.printStackTrace();
        }
        return initialWeapons;
    }
}