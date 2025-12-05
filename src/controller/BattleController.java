package controller;

import model.Party;
import model.entity.*;
import model.item.Item;
import model.item.Spell;
import util.GameConfig;
import view.Colors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Handles the Turn-Based Combat State.
 * delegates Inventory actions to InventoryController.
 * * @author Serena N.
 * @version 3.0 (Refactored)
 */
public class BattleController {
    private Scanner scanner;
    private Party party;
    private List<Monster> monsters;
    private int roundCounter;
    private boolean battleActive;

    // Sub-Controllers
    private InventoryController inventoryController;
    private HeroController heroController;

    // Constructor accepts Scanner to share input stream
    public BattleController(Scanner scanner, InventoryController inventoryController, HeroController heroController) {
        this.scanner = scanner;
        this.inventoryController = new InventoryController(scanner);
        this.heroController = new HeroController();
    }

    public void startBattle(Party party, List<Monster> allPossibleMonsters) {
        this.party = party;
        this.monsters = generateMonsters(allPossibleMonsters, party.getSize());
        this.battleActive = true;

        System.out.println(Colors.RED + "\n!!! A BATTLE HAS STARTED !!!" + Colors.RESET);
        System.out.println("You encountered " + monsters.size() + " monsters!");

        while (battleActive) {
            playRound();
        }
    }

    private List<Monster> generateMonsters(List<Monster> pool, int count) {
        List<Monster> enemies = new ArrayList<>();
        Random rand = new Random();

        int currentLevel = 1;
        for (Hero h : party.getHeroes()) if (h.getLevel() > currentLevel) currentLevel = h.getLevel();

        int searchLevel = Math.min(currentLevel, util.GameConfig.MAX_LEVEL);

        List<Monster> validMonsters = new ArrayList<>();
        for (Monster m : pool) if (m.getLevel() == searchLevel) validMonsters.add(m);

        if (validMonsters.isEmpty()) validMonsters = pool; // Fallback
        Collections.shuffle(validMonsters);

        for (int i = 0; i < count; i++) {
            Monster template = (i < validMonsters.size()) ? validMonsters.get(i) : validMonsters.get(rand.nextInt(validMonsters.size()));

            Monster newMonster;
            if (template instanceof Dragon) newMonster = new Dragon(template.getName(), template.getLevel(), template.getBaseDamage(), template.getDefense(), template.getDodgeChance());
            else if (template instanceof Exoskeleton) newMonster = new Exoskeleton(template.getName(), template.getLevel(), template.getBaseDamage(), template.getDefense(), template.getDodgeChance());
            else newMonster = new Spirit(template.getName(), template.getLevel(), template.getBaseDamage(), template.getDefense(), template.getDodgeChance());

            newMonster.scaleStats(currentLevel);
            enemies.add(newMonster);
        }
        return enemies;
    }

    private void playRound() {
        System.out.println("\n--- NEW ROUND ---");
        printRoundStatus();

        // Hero Turns
        for (Hero hero : party.getHeroes()) {
            if (hero.isFainted()) continue;
            if (!battleActive) break;
            heroTurn(hero);
            checkWinCondition();
        }

        // Pause
        if (battleActive) {
            System.out.println("\n(Heroes finished. Press Enter...)");
            try { System.in.read(); } catch (Exception e) {}
        }

        // Monster Turns
        if (battleActive) {
            System.out.println("\n--- MONSTERS ATTACK! ---");
            for (Monster monster : monsters) {
                if (monster.isFainted()) continue;
                monsterTurn(monster);
                checkLossCondition();
                if (!battleActive) break;
            }
        }

        // Regenerate
        if (battleActive) {
            for (Hero h : party.getHeroes()) {
                if (!h.isFainted()) {
                    // Calculate amounts based on Config
                    double hpGain = h.getHp() * (util.GameConfig.REGEN_RATE - 1.0); // e.g. 100 * 0.1 = 10
                    double mpGain = h.getMana() * (util.GameConfig.REGEN_RATE - 1.0);
                    // Apply updates
                    h.setHp(h.getHp() + hpGain);
                    h.setMana(h.getMana() + mpGain);
                    System.out.printf(" %s regained %d HP and %d Mana.\n",
                            h.getName(), (int)hpGain, (int)mpGain);
                }
            }
            //System.out.println("Heroes regained HP/Mana.");
        }
    }

    // --- HERO ACTIONS ---
    private void heroTurn(Hero hero) {
        boolean validAction = false;
        while (!validAction) {
            System.out.println("\n========================================");
            System.out.println(Colors.CYAN + " CURRENT TURN: " + hero.getName() + Colors.RESET);
            System.out.printf(" HP: %-4d / %-4d  |  MP: %-4d\n",
                    (int)hero.getHp(), (int)(hero.getLevel() * 100), (int)hero.getMana());

            // Mini-Map of Enemies (Quick view)
            System.out.println("\n VS ENEMIES:");
            for (int i = 0; i < monsters.size(); i++) {
                Monster m = monsters.get(i);
                if (!m.isFainted()) {
                    System.out.printf(" %d. %-15s [HP: %-3d]\n", (i + 1), m.getName(), (int)m.getHp());
                } else {
                    System.out.println(Colors.RED + " " + (i + 1) + ". " + m.getName() + " (Defeated)" + Colors.RESET);
                }
            }
            System.out.println("========================================");

            // --- SEPARATE LINES FOR COMMANDS ---
            System.out.println("Choose an action:");
            System.out.println("  1. Attack");
            System.out.println("  2. Cast Spell");
            System.out.println("  3. Use Potion");
            System.out.println("  4. Equip Weapon/Armor");
            System.out.println("  5. View Hero Stats");
            System.out.println("  6. View Monster Stats");
            System.out.println("  Q. Quit Game");
            System.out.print("> ");

            String input = scanner.next().toUpperCase();

            switch (input) {
                case "1": validAction = attackMonster(hero); break;
                case "2": validAction = castSpell(hero); break;
                case "3": validAction = inventoryController.openPotionMenu(hero); break;
                case "4": validAction = inventoryController.openEquipMenu(hero); break;
                case "5": showHeroStats(); break;
                case "6": showMonsterStats(); break;
                case "Q": System.exit(0); break;
                default: System.out.println("Invalid action.");
            }
        }
    }

    private boolean attackMonster(Hero hero) {
        Monster target = selectMonster();
        if (target == null) return false;

        if (Math.random() < target.getDodgeChance() * 0.01) {
            System.out.println(target.getName() + " dodged the attack from " + hero.getName() + "!");
        } else {
            double damage = heroController.calculateDamage(hero);
            double actualDmg = Math.max(0, damage - (target.getDefense() * 0.02));
            target.takeDamage(actualDmg);
            System.out.println(hero.getName() + " dealt " + (int)actualDmg + " damages to " + target.getName() + ".");
        }
        return true;
    }

    private boolean castSpell(Hero hero) {
        // 1. Filter Spells (Keep this UI logic here or move to InventoryController helper)
        List<Spell> spells = new ArrayList<>();
        for (Item i : hero.getInventory()) if (i instanceof Spell) spells.add((Spell) i);

        if (spells.isEmpty()) { System.out.println("No spells."); return false; }

        for (int i = 0; i < spells.size(); i++) {
            System.out.printf("%d. %s (Mana: %.0f)\n", (i+1), spells.get(i).getName(), spells.get(i).getManaCost());
        }
        System.out.print("Select Spell (0 cancel): ");

        if(!scanner.hasNextInt()) { scanner.next(); return false; }
        int idx = scanner.nextInt();
        if (idx <= 0 || idx > spells.size()) return false;

        Spell s = spells.get(idx-1);

        // 2. Check Requirements (Model Query)
        if (hero.getMana() < s.getManaCost()) {
            System.out.println("Not enough Mana.");
            return false;
        }

        Monster target = selectMonster();
        if (target == null) return false;

        // 3. Execute (Update Model)
        hero.setMana(hero.getMana() - s.getManaCost());

        // --- DELEGATE MATH TO HERO CONTROLLER ---
        double damage = heroController.calculateSpellDamage(hero, s);

        target.takeDamage(damage);
        System.out.println("Cast " + s.getName() + " for " + (int)damage + " damage.");

        // Spell Effects (Logic stays here or moves to Spell class strategy)
        if (s.getType() == Spell.SpellType.ICE) target.setBaseDamage(target.getBaseDamage() * 0.9);
        if (s.getType() == Spell.SpellType.FIRE) target.setDefense(target.getDefense() * 0.9);
        if (s.getType() == Spell.SpellType.LIGHTNING) target.setDodgeChance(target.getDodgeChance() * 0.9);

        // --- DELEGATE REMOVAL TO INVENTORY CONTROLLER ---
        inventoryController.consumeItem(hero, s);

        return true;
    }

    // --- HELPER METHODS ---
    private Monster selectMonster() {
        System.out.println("Target:");
        List<Monster> live = new ArrayList<>();
        for(Monster m : monsters) if(!m.isFainted()) live.add(m);
        if(live.isEmpty()) return null;

        for(int i=0; i<live.size(); i++) System.out.printf("%d. %s (HP: %.0f)\n", (i+1), live.get(i).getName(), live.get(i).getHp());
        if(!scanner.hasNextInt()) { scanner.next(); return null; }
        int choice = scanner.nextInt();
        return (choice > 0 && choice <= live.size()) ? live.get(choice-1) : null;
    }

    private void monsterTurn(Monster monster) {
        List<Hero> targets = new ArrayList<>();
        for(Hero h : party.getHeroes()) if(!h.isFainted()) targets.add(h);
        if(targets.isEmpty()) return;

        Hero target = targets.get(new Random().nextInt(targets.size()));

        //Use HeroController to calculate the chance
        double dodgeChance = heroController.calculateDodgeChance(target);

        if (Math.random() < dodgeChance) {
            System.out.println(monster.getName() + " attacked " + target.getName()
                    + " -> BUT MISSED! " + target.getName() + " dodged the attack!");
        } else {
            double incomingDmg = monster.getBaseDamage();

            // Armor reduction logic
            // (This logic is simple enough to stay here, or you could move 'calculateDefense' to HeroController too)
            double defense = (target.getEquippedArmor() != null) ? target.getEquippedArmor().getDamageReduction() : 0;
            double actualDmg = Math.max(0, incomingDmg - defense);

            target.takeDamage(actualDmg);

            if (actualDmg == 0 && defense > 0) {
                System.out.println(monster.getName() + " attacked " + target.getName() + " -> BLOCKED by Armor!");
            } else {
                System.out.println(monster.getName() + " hit " + target.getName() + " for " + (int)actualDmg + " damages.");
            }
        }
    }

    private void printRoundStatus() {
        System.out.println("\nSTATUS REPORT:");
        for (int i = 0; i < party.getSize(); i++) {
            Hero h = party.getHero(i);
            String s = h.isFainted() ? "FAINTED" : "Ready";
            System.out.printf("%s: HP %.0f/%.0f [%s]\n", h.getName(), h.getHp(), h.getLevel()*100.0, s);
        }
        for (Monster m : monsters) {
            if (!m.isFainted()) System.out.printf("VS %s (HP: %.0f)\n", m.getName(), m.getHp());
        }
    }

    // --- INFO DISPLAYS ---
    private void showHeroStats() {
        System.out.println("\n================== PARTY STATUS ==================");

        for (int i = 0; i < party.getSize(); i++) {
            Hero h = party.getHero(i);

            // Header with Status
            String statusTag = h.isFainted() ? (Colors.RED + "[FAINTED]" + Colors.RESET) : "[ALIVE]";
            System.out.printf(" %d. %s %s (Level %d %s)\n",
                    (i + 1), h.getName(), statusTag, h.getLevel(), h.getClass().getSimpleName());

            // Stats Grid
            System.out.printf("    HP: %-5.0f/ %-5.0f | MP: %-5.0f\n",
                    h.getHp(), (double)(h.getLevel() * 100), h.getMana());

            System.out.printf("    Str: %-4.0f | Dex: %-4.0f | Agi: %-4.0f\n",
                    h.getStrength(), h.getDexterity(), h.getAgility());

            System.out.printf("    Gold: %-5.0f | XP: %-5.0f\n",
                    h.getGold(), h.getExperience());

            // COMBAT STATS
            System.out.println("    Combat Power");

            // Calculate Effective Defense from Armor
            double armorDef = (h.getEquippedArmor() != null) ? h.getEquippedArmor().getDamageReduction() : 0;

            // Calculate Total Damage (Strength + Weapon) using the Hero's logic method
            double totalDmg = heroController.calculateDamage(h);

            // Calculate Dodge %
            double dodgeChance = heroController.calculateDodgeChance(h) * 100;

            System.out.printf("     Damage:  %-5.0f (Str + Weapon)\n", totalDmg);
            System.out.printf("     Defense: %-5.0f (Armor)\n", armorDef);
            System.out.printf("     Dodge:   %-5.0f%%\n", dodgeChance);

            // Gear
            System.out.println("    Equipped Gear");
            if (h.getEquippedWeapon() != null) {
                System.out.printf("     Weapon: %s (Val: %.0f)\n", h.getEquippedWeapon().getName(), h.getEquippedWeapon().getDamage());
            } else {
                System.out.println("     Weapon: None");
            }

            if (h.getEquippedArmor() != null) {
                System.out.printf("   Armor:  %s (Val: %.0f)\n", h.getEquippedArmor().getName(), h.getEquippedArmor().getDamageReduction());
            } else {
                System.out.println("   Armor:  None");
            }

            // Inventory Listing
            System.out.println("    Inventory:");
            if (h.getInventory().isEmpty()) {
                System.out.println("      (Empty)");
            } else {
                for (model.item.Item item : h.getInventory()) {
                    // Print Item Name and Level requirement
                    System.out.printf("      - %-18s (Lvl %d)\n", item.getName(), item.getMinLevel());
                }
            }

            // Separator
            if (i < party.getSize() - 1) {
                System.out.println(" --------------------------------------------------");
            }
        }
        System.out.println("==================================================");

        System.out.println("(Press Enter to return...)");
        try { System.in.read(); } catch (Exception e) {}
    }

    private void showMonsterStats() {
        System.out.println("\n================== MONSTER INTEL ==================");

        for (int i = 0; i < monsters.size(); i++) {
            Monster m = monsters.get(i);

            // Handle Defeated Monsters
            if (m.isFainted()) {
                System.out.println(Colors.RED + " " + (i + 1) + ". " + m.getName() + " [DEFEATED]" + Colors.RESET);
                System.out.println("===================================================");
                continue;
            }

            // 1. Identity Line
            String type = m.getClass().getSimpleName(); // "Dragon", "Spirit", etc.
            System.out.printf(" %d. " + Colors.RED + "%-20s" + Colors.RESET + " (Level %d %s)\n",
                    (i + 1), m.getName(), m.getLevel(), type);

            System.out.println(" --------------------------------------------------");

            // 2. Stats Grid
            // HP Row
            System.out.printf("   HP:       %-5.0f\n", m.getHp());

            // Combat Stats Row
            System.out.printf("   Damage:   %-5.0f  |  Defense:  %-5.0f\n", m.getBaseDamage(), m.getDefense());

            // Dodge Row
            System.out.printf("   Dodge:    %-5.0f%%\n", (m.getDodgeChance() * 100));

            System.out.println("===================================================");
        }

        System.out.println("(Press Enter to return...)");
        try { System.in.read(); } catch (Exception e) {}
    }

    // --- END CONDITIONS ---
    private void checkWinCondition() {
        boolean allDead = true;
        for (Monster m : monsters) if (!m.isFainted()) allDead = false;

        if (allDead) {
            System.out.println(Colors.GREEN + "VICTORY!" + Colors.RESET);
            printBattleSummary(Colors.GREEN + "HEROES" + Colors.RESET);

            double goldReward = monsters.get(0).getLevel() * 100;
            double xpReward = monsters.size() * 2;

            for (Hero h : party.getHeroes()) {
                if (!h.isFainted()) {
                    h.addGold(goldReward);
                    heroController.gainExperience(h, xpReward);
                    // Check Game Over
                    if (h.getLevel() > GameConfig.MAX_LEVEL) {
                        System.out.println("CONGRATULATIONS! You reached Level 11. Game Over.");
                        System.exit(0);
                    }
                } else {
                    System.out.println(h.getName() + " revived.");
                    h.setHp(h.getLevel() * GameConfig.REVIVE_RATE);
                    h.setMana(h.getLevel() * GameConfig.REVIVE_RATE);
                }
            }
            battleActive = false;
        }
    }

    private void checkLossCondition() {
        if(party.isPartyFainted()) {
            printBattleSummary(Colors.RED + "MONSTERS" + Colors.RESET);
            System.out.println(Colors.RED + "DEFEAT." + Colors.RESET);
            System.exit(0);
        }
    }

    private void printBattleSummary(String winner) {
        System.out.println("\n========================================");
        System.out.println("           BATTLE FINISHED              ");
        System.out.println("========================================");
        System.out.println(" Winner:        " + winner);
        System.out.println(" Total Rounds:  " + roundCounter);
        System.out.println("----------------------------------------");
        System.out.println(" PARTY STATUS:");
        for (int i = 0; i < party.getSize(); i++) {
            Hero h = party.getHero(i);
            String status = h.isFainted() ? "Fainted" : "Alive";
            System.out.printf(" - %-15s (Lvl %d) [%s]\n", h.getName(), h.getLevel(), status);
        }
        System.out.println("========================================\n");
    }
}