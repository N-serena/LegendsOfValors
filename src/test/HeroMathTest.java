package test;

import controller.HeroController;
import model.entity.Warrior;
import util.GameConfig;

/**
 * A "Driver Class" to verify game math logic.
 * Run this file SEPARATELY from Main.java to check your formulas.
 */
public class HeroMathTest {

    public static void main(String[] args) {
        System.out.println("Starting Unit Tests...");
        System.out.println("--------------------------------------------------");

        // 1. ARRANGE: Setup the Controller and a Dummy Hero
        HeroController controller = new HeroController();

        // Create a Warrior with: 100 Mana, 100 Str, 100 Agi, 100 Dex, 0 Gold, 0 XP
        Warrior testHero = new Warrior("TestDummy", 100, 100, 100, 100, 0, 0);

        // ---------------------------------------------------------------
        // TEST CASE 1: Damage Calculation
        // ---------------------------------------------------------------
        // Rule: (Strength + WeaponDamage) * DAMAGE_SCALE
        // Config: DAMAGE_SCALE is typically 0.05
        // Math: (100 + 0) * 0.05 = 5.0

        System.out.print("Test 1 (Damage Calc): ");
        double expectedDamage = 5.0;
        double actualDamage = controller.calculateDamage(testHero);

        assertResult(expectedDamage, actualDamage);

        // ---------------------------------------------------------------
        // TEST CASE 2: Dodge Calculation
        // ---------------------------------------------------------------
        // Rule: Agility * DODGE_SCALE
        // Config: DODGE_SCALE is 0.002 (0.2%)
        // Math: 100 * 0.002 = 0.2 (20% chance)

        System.out.print("Test 2 (Dodge Calc):  ");
        double expectedDodge = 0.2;
        double actualDodge = controller.calculateDodgeChance(testHero);

        assertResult(expectedDodge, actualDodge);

        // ---------------------------------------------------------------
        // TEST CASE 3: Dodge Cap Limit
        // ---------------------------------------------------------------
        // Rule: Dodge should never exceed MAX_DODGE_CHANCE (0.5)
        // Setup: Give hero 1,000,000 Agility (God Mode)
        testHero.setAgility(1000000);

        System.out.print("Test 3 (Dodge Cap):   ");
        double expectedCap = 0.5;
        double actualCap = controller.calculateDodgeChance(testHero);

        assertResult(expectedCap, actualCap);

        System.out.println("--------------------------------------------------");
        System.out.println("Tests Complete.");
    }

    // Helper method to compare numbers and print Green/Red results
    private static void assertResult(double expected, double actual) {
        // We use a small "epsilon" (0.0001) because double math is slightly imprecise
        if (Math.abs(expected - actual) < 0.0001) {
            System.out.println("PASS [Expected: " + expected + " | Actual: " + actual + "]");
        } else {
            System.out.println("FAIL [Expected: " + expected + " | Actual: " + actual + "]");
        }
    }
}