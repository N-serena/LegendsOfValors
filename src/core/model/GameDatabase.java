package core.model;

import core.model.entity.Hero;
import core.model.entity.Monster;
import core.model.item.Item;
import core.model.item.spell.Spell;
import core.util.GameDataParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameDatabase {

    // 1. The Single Instance
    private static GameDatabase instance;

    // 2. The Shared Data
    private List<Hero> allHeroes;
    private List<Monster> allMonsters;
    private List<Item> allItems;

    // 3. Private Constructor (Prevents 'new GameDatabase()')
    private GameDatabase() {
        this.allHeroes = new ArrayList<>();
        this.allMonsters = new ArrayList<>();
        this.allItems = new ArrayList<>();
    }

    // 4. Global Access Point
    public static GameDatabase getInstance() {
        if (instance == null) {
            instance = new GameDatabase();
        }
        return instance;
    }

    // 5. The Initialization Logic (Moved here from MHGameController)
    public void loadData() {
        if (!allHeroes.isEmpty()) return; // Prevent double loading

        try {
            System.out.println("Loading game assets from Singleton Database...");
            // Heroes
            allHeroes.addAll(GameDataParser.parseHeroes("data_files/Warriors.txt", "Warrior"));
            allHeroes.addAll(GameDataParser.parseHeroes("data_files/Sorcerers.txt", "Sorcerer"));
            allHeroes.addAll(GameDataParser.parseHeroes("data_files/Paladins.txt", "Paladin"));

            // Monsters
            allMonsters.addAll(GameDataParser.parseMonsters("data_files/Dragons.txt", "Dragon"));
            allMonsters.addAll(GameDataParser.parseMonsters("data_files/Exoskeletons.txt", "Exoskeleton"));
            allMonsters.addAll(GameDataParser.parseMonsters("data_files/Spirits.txt", "Spirit"));

            // Items
            allItems.addAll(GameDataParser.parseWeapons("data_files/Weaponry.txt"));
            allItems.addAll(GameDataParser.parseArmor("data_files/Armory.txt"));
            allItems.addAll(GameDataParser.parsePotions("data_files/Potions.txt"));
            allItems.addAll(GameDataParser.parseSpells("data_files/IceSpells.txt", Spell.SpellType.ICE));
            allItems.addAll(GameDataParser.parseSpells("data_files/FireSpells.txt", Spell.SpellType.FIRE));
            allItems.addAll(GameDataParser.parseSpells("data_files/LightningSpells.txt", Spell.SpellType.LIGHTNING));

        } catch (IOException e) {
            System.err.println("Critical Error loading files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 6. Getters (Return unmodifiable lists to protect data)
    public List<Hero> getAllHeroes() { return Collections.unmodifiableList(allHeroes); }
    public List<Monster> getAllMonsters() { return Collections.unmodifiableList(allMonsters); }
    public List<Item> getAllItems() { return Collections.unmodifiableList(allItems); }
}