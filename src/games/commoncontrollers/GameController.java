package games.commoncontrollers;

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
        System.out.println("Loading game assets...");
        allHeroes.addAll(GameDataParser.parseHeroes("data_files/Warriors.txt", "Warrior"));
        allHeroes.addAll(GameDataParser.parseHeroes("data_files/Sorcerers.txt", "Sorcerer"));
        allHeroes.addAll(GameDataParser.parseHeroes("data_files/Paladins.txt", "Paladin"));

        allMonsters.addAll(GameDataParser.parseMonsters("data_files/Dragons.txt", "Dragon"));
        allMonsters.addAll(GameDataParser.parseMonsters("data_files/Exoskeletons.txt", "Exoskeleton"));
        allMonsters.addAll(GameDataParser.parseMonsters("data_files/Spirits.txt", "Spirit"));

        allItems.addAll(GameDataParser.parseWeapons("data_files/Weaponry.txt"));
        allItems.addAll(GameDataParser.parseArmor("data_files/Armory.txt"));
        allItems.addAll(GameDataParser.parsePotions("data_files/Potions.txt"));
        allItems.addAll(GameDataParser.parseSpells("data_files/IceSpells.txt", Spell.SpellType.ICE));
        allItems.addAll(GameDataParser.parseSpells("data_files/FireSpells.txt", Spell.SpellType.FIRE));
        allItems.addAll(GameDataParser.parseSpells("data_files/LightningSpells.txt", Spell.SpellType.LIGHTNING));
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
            initialWeapons = GameDataParser.parseWeapons("datafiles/Initial.txt");
        }
        catch(IOException IO)
        {
            IO.printStackTrace();
        }
        return initialWeapons;
    }
}
