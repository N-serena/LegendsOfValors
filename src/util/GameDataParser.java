package util;

import model.entity.*;
import model.item.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing game data from text files.
 ** @author Serena N
 * @version 1.0
 */
public class GameDataParser {

    // --- PARSE ITEMS ---
    public static List<Item> parseWeapons(String filePath) throws IOException {
        List<Item> items = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = reader.readLine(); // Skip Header

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 5) continue; // Ensure valid row

            // File format: Name/cost/level/damage/required hands
            String name = parts[0];
            double cost = Double.parseDouble(parts[1]);
            int level = Integer.parseInt(parts[2]);
            double damage = Double.parseDouble(parts[3]);
            int hands = Integer.parseInt(parts[4]);

            items.add(new Weapon(name, cost, level, damage, hands));
        }
        reader.close();
        return items;
    }

    public static List<Item> parseArmor(String filePath) throws IOException {
        List<Item> items = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = reader.readLine();

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 4) continue;

            // File format: Name/cost/required level/damage reduction
            items.add(new Armor(parts[0], Double.parseDouble(parts[1]),
                    Integer.parseInt(parts[2]), Double.parseDouble(parts[3])));
        }
        reader.close();
        return items;
    }

    public static List<Item> parsePotions(String filePath) throws IOException {
        List<Item> items = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = reader.readLine();

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 4) continue;

            // File format: Name/cost/required level/attribute increase/attribute affected
            // "Mermaid_Tears" case might have index 4 be "Health/Mana..."
            String affected = parts[4];
            items.add(new Potion(parts[0], Double.parseDouble(parts[1]),
                    Integer.parseInt(parts[2]), Double.parseDouble(parts[3]), affected));
        }
        reader.close();
        return items;
    }

    public static List<Item> parseSpells(String filePath, Spell.SpellType type) throws IOException {
        List<Item> items = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = reader.readLine();

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 5) continue;

            // File format: Name/cost/required level/damage/mana cost
            items.add(new Spell(parts[0], Double.parseDouble(parts[1]),
                    Integer.parseInt(parts[2]), Double.parseDouble(parts[3]),
                    Double.parseDouble(parts[4]), type));
        }
        reader.close();
        return items;
    }

    // --- PARSE ENTITIES ---

    public static List<Hero> parseHeroes(String filePath, String type) throws IOException {
        List<Hero> heroes = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = reader.readLine();

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 7) continue;

            // File: Name/mana/strength/agility/dexterity/starting money/starting experience
            String name = parts[0];
            double mana = Double.parseDouble(parts[1]);
            double str = Double.parseDouble(parts[2]);
            double agi = Double.parseDouble(parts[3]);
            double dex = Double.parseDouble(parts[4]);
            double money = Double.parseDouble(parts[5]);
            double exp = Double.parseDouble(parts[6]);

            if (type.equalsIgnoreCase("Warrior")) heroes.add(new Warrior(name, mana, str, agi, dex, money, exp));
            else if (type.equalsIgnoreCase("Sorcerer")) heroes.add(new Sorcerer(name, mana, str, agi, dex, money, exp));
            else if (type.equalsIgnoreCase("Paladin")) heroes.add(new Paladin(name, mana, str, agi, dex, money, exp));
        }
        reader.close();
        return heroes;
    }

    public static List<Monster> parseMonsters(String filePath, String type) throws IOException {
        List<Monster> monsters = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = reader.readLine();

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 5) continue;

            // File: Name/level/damage/defense/dodge chance
            String name = parts[0];
            int level = Integer.parseInt(parts[1]);
            double dmg = Double.parseDouble(parts[2]);
            double def = Double.parseDouble(parts[3]);
            double dodge = Double.parseDouble(parts[4]);

            if (type.equalsIgnoreCase("Dragon")) monsters.add(new Dragon(name, level, dmg, def, dodge));
            else if (type.equalsIgnoreCase("Exoskeleton")) monsters.add(new Exoskeleton(name, level, dmg, def, dodge));
            else if (type.equalsIgnoreCase("Spirit")) monsters.add(new Spirit(name, level, dmg, def, dodge));
        }
        reader.close();
        return monsters;
    }
}