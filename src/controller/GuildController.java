package controller;

import model.Party;
import model.entity.Hero;
import util.GameConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Manages the "Mercenary Guild" logic where players can swap heroes.
 * Handles scarcity (swapsLeft), cost calculation, and hero instantiation.
 * * @author Serena N.
 * @version 1.0
 */
public class GuildController {
    private Scanner scanner;
    private int swapsLeft;
    private HeroController heroController = new HeroController();

    public GuildController(Scanner scanner) {
        this.scanner = scanner;
        this.swapsLeft = GameConfig.GUILD_MAX_SWAPS;
    }

    /**
     * Main entry point for the Guild menu.
     * @param party The current player party.
     * @param allHeroes The master list of all available hero templates.
     */
    public void enterGuild(Party party, List<Hero> allHeroes) {
        System.out.println("\n================= MERCENARY GUILD =================");

        // 1. Check Scarcity Rule
        if (swapsLeft <= 0) {
            System.out.println("Guild Master: \"You have already used your swap contract! Be gone.\"");
            return;
        }

        // 2. Calculate Cost based on Party Level
        int maxLevel = 0;
        for (int i = 0; i < party.getSize(); i++) {
            Hero h = party.getHero(i);
            if (h.getLevel() > maxLevel) maxLevel = h.getLevel();
        }
        int cost = maxLevel * GameConfig.GUILD_COST_PER_LEVEL;

        System.out.println("Guild Master: \"Want to replace a hero? It will cost " + cost + " Gold.\"");
        System.out.println("1. Proceed | 0. Cancel");

        if (!scanner.hasNextInt() || scanner.nextInt() != 1) return;

        // 3. Logic: Select Payer, Fire Hero, Hire Hero
        if (!performSwap(party, allHeroes, cost, maxLevel)) {
            System.out.println("Transaction cancelled.");
        }
    }

    private boolean performSwap(Party party, List<Hero> allHeroes, int cost, int targetLevel) {
        // A. Select Payer
        System.out.println("\nWho is paying the fee?");
        for (int i = 0; i < party.getSize(); i++) {
            System.out.printf("%d. %s (Gold: %.0f)\n", (i+1), party.getHero(i).getName(), party.getHero(i).getGold());
        }
        int payerIdx = getValidIntInput(party.getSize()) - 1;
        if (payerIdx < 0) return false;

        Hero payer = party.getHero(payerIdx);
        if (payer.getGold() < cost) {
            System.out.println("Guild Master: \"You can't afford my services!\"");
            return false;
        }

        // B. Select Hero to Dismiss
        System.out.println("\nWho are we firing?");
        for (int i = 0; i < party.getSize(); i++) {
            System.out.printf("%d. %s (Level %d)\n", (i+1), party.getHero(i).getName(), party.getHero(i).getLevel());
        }
        int fireIdx = getValidIntInput(party.getSize()) - 1;
        if (fireIdx < 0) return false;

        // C. Select Replacement
        System.out.println("\nSelect a replacement:");
        List<Hero> candidates = new ArrayList<>();
        for (Hero h : allHeroes) {
            boolean inParty = false;
            // Check if hero is already in party to prevent duplicates
            for (int i = 0; i < party.getSize(); i++) {
                if (party.getHero(i).getName().equals(h.getName())) inParty = true;
            }
            if (!inParty) candidates.add(h);
        }

        for (int i = 0; i < candidates.size(); i++) {
            System.out.printf("%d. %s (%s)\n", (i+1), candidates.get(i).getName(), candidates.get(i).getClass().getSimpleName());
        }

        int recruitIdx = getValidIntInput(candidates.size()) - 1;
        if (recruitIdx < 0) return false;

        // D. Execute
        Hero newHero = candidates.get(recruitIdx);

        // Scale new hero to match the party level
        while (newHero.getLevel() < targetLevel) {
            heroController.gainExperience(newHero, newHero.getLevel() * 10 + 1);
        }
        newHero.setHp(newHero.getLevel() * 100);

        payer.decreaseGold(cost);
        party.replaceHero(fireIdx, newHero);
        swapsLeft--;

        System.out.println("Transaction Complete! " + newHero.getName() + " has joined the party.");
        return true;
    }

    // Helper for input validation
    private int getValidIntInput(int max) {
        System.out.print("> ");
        if (scanner.hasNextInt()) {
            int val = scanner.nextInt();
            if (val > 0 && val <= max) return val;
        } else {
            scanner.next();
        }
        return -1;
    }
}