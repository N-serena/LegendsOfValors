package games.legendsofvalors.model;

import core.interfaces.HeroObserver;
import core.model.entity.Hero;
import core.util.Colors;

/**
 * a hero class used for Legends of Valor, extends HeroDecorator
 */
public class ValorHero extends HeroDecorator implements HeroObserver {

    // LOV Specific Attributes
    private int nexusRow;
    private int nexusCol;
    private String lane; // "Top", "Mid", "Bot"
    private int currentRow;
    private int currentCol;

//    public ValorHero(String name, double mana, double str, double agi, double dex, double money, double xp) {
//        super(name, mana, str, agi, dex, money, xp);
//        // Default values
//        this.nexusRow = -1;
//        this.nexusCol = -1;
//        this.lane = "Unassigned";
//    }

    public ValorHero(Hero hero) {
        super(hero);
        this.nexusRow = -1;
        this.nexusCol = -1;
        this.lane = "Unassigned";
    }

//    public Hero getHero() {
//        return hero;
//    }


    // --- RESPAWN & RECALL ---
    public void setHomeNexus(int row, int col, String lane) {
        this.nexusRow = row;
        this.nexusCol = col;
        this.lane = lane;
    }

    /**
     * Teleports the hero back to their assigned Nexus.
     * Used for the "Recall" command AND when Respawning after death.
     */
    public void regenerateStats() {
        if (nexusRow != -1 && nexusCol != -1) {
            // NOTE: We will need to talk to the Board to move visually,
            // but for now, we reset the internal state.
            setHp(this.hero.getLevel()*100);// Reset HP to Max (Example Formula)
            setMana(this.hero.getLevel() * 100); // Reset Mana to Max
        }
    }

    /**
     * A hero gets a reward when that hero or another hero in the party has defeated a monster
     */

    public void getReward(int level)
    {
        this.hero.addGold(level * 500);
        System.out.println(name + " gained " + (level * 500) + " gold!");
        this.hero.addExperience(level * 2);
        System.out.println(name + " gained " + level * 2 + " EXP!");
        System.out.println();
        if (this.hero.getExperience() >= this.hero.getLevel() * 10)
        {
            levelUp();
        }
    }

    @Override
    public void levelUp() {
        super.levelUp();
        System.out.println();
        System.out.println(Colors.GREEN + this.name + " has leveled up to " + this.hero.getLevel() + " in Legends of Valors!" + Colors.RESET);
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Lane: %s | Nexus: (%d, %d)", lane, nexusRow, nexusCol);
    }

    public int getNexusRow() { return nexusRow; }
    public int getNexusCol() { return nexusCol; }
    public int getCurrentRow() { return currentRow; }
    public int getCurrentPos() { return currentCol; }
    public String getLane() { return lane; }

    public int getLevel()
    {
        return this.hero.getLevel();
    }
    public double getGold()
    {
        return this.hero.getGold();
    }
    public double getMana()
    {
        return this.hero.getMana();
    }

//    public int getLevel()
//    {
//        return this.hero.getLevel();
//    }
}