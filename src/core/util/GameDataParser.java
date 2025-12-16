package core.util;

import core.model.entity.*;
import core.model.item.*;
import core.model.item.spell.FireSpell;
import core.model.item.spell.IceSpell;
import core.model.item.spell.LightningSpell;
import core.model.item.spell.Spell;
import games.legendsofvalors.model.ValorHero;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Utility class for parsing game data from text files.
 ** @author Serena N
 * @version 1.0
 */
public class GameDataParser {

    // --- PARSE ITEMS ---
    public static List<Item> parseWeapons(String filePath) throws IOException {
        List<Item> items = new ArrayList<>();
        BufferedReader reader = newReader(filePath);
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
        BufferedReader reader = newReader(filePath);
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
        BufferedReader reader = newReader(filePath);
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
        BufferedReader reader = newReader(filePath);
        String line = reader.readLine();

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 5) continue;

            if (type == Spell.SpellType.valueOf("FIRE")) {
                        items.add(new FireSpell(parts[0], Double.parseDouble(parts[1]),
                        Integer.parseInt(parts[2]), Double.parseDouble(parts[3]),
                        Double.parseDouble(parts[4]), type));
            }
            else if (type == Spell.SpellType.valueOf("LIGHTNING")) {
                items.add(new LightningSpell(parts[0], Double.parseDouble(parts[1]),
                        Integer.parseInt(parts[2]), Double.parseDouble(parts[3]),
                        Double.parseDouble(parts[4]), type));
            }
            else if (type == Spell.SpellType.valueOf("ICE")){
                items.add(new IceSpell(parts[0], Double.parseDouble(parts[1]),
                        Integer.parseInt(parts[2]), Double.parseDouble(parts[3]),
                        Double.parseDouble(parts[4]), type));
            }
        }

        reader.close();
        return items;
    }

    // --- PARSE ENTITIES ---

    public static List<Hero> parseHeroes(String filePath, String type) throws IOException {
        List<Hero> heroes = new ArrayList<>();
        BufferedReader reader = newReader(filePath);
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
        BufferedReader reader = newReader(filePath);
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
    // Resolve data file paths even when the JVM launches from a higher-level directory.
    private static BufferedReader newReader(String filePath) throws IOException {
        Path resolved = resolveDataFile(filePath);
        return new BufferedReader(new FileReader(resolved.toFile()));
    }

    private static Path resolveDataFile(String filePath) throws IOException {
        Path candidate = Paths.get(filePath);
        if (Files.exists(candidate)) {
            return candidate;
        }

        Path cwd = Paths.get(System.getProperty("user.dir"));
        candidate = cwd.resolve(filePath);
        if (Files.exists(candidate)) {
            return candidate;
        }

        Path dataDir = locateDataDirectory(cwd);
        if (dataDir != null) {
            Path relative = Paths.get(filePath);
            if (relative.getNameCount() > 1 && "data_files".equals(relative.getName(0).toString())) {
                Path resolved = dataDir.resolve(relative.subpath(1, relative.getNameCount()));
                if (Files.exists(resolved)) {
                    return resolved;
                }
            }

            Path resolved = dataDir.resolve(relative.getFileName());
            if (Files.exists(resolved)) {
                return resolved;
            }
        }

        throw new FileNotFoundException("Unable to locate data file: " + filePath);
    }

    private static Path locateDataDirectory(Path startDir) throws IOException {
        Path candidate = startDir.resolve("data_files");
        if (Files.isDirectory(candidate)) {
            return candidate;
        }

        try (Stream<Path> stream = Files.walk(startDir, 3)) {
            return stream.filter(p -> Files.isDirectory(p) && "data_files".equals(p.getFileName().toString()))
                    .findFirst()
                    .orElse(null);
        }
    }
}