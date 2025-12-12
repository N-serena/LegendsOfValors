package games.legendsofvalors.model;

import core.interfaces.HeroObserver;
import core.model.entity.Hero;

public class ValorHero extends Hero implements HeroObserver {

    // LOV Specific Attributes
    private int nexusRow;
    private int nexusCol;
    private String lane; // "Top", "Mid", "Bot"
    private int currentRow;
    private int currentCol;

    // Constructor: Matches the config file format but adds LoV defaults
    public ValorHero(String name, int mana, int str, int agi, int dex, int money, int xp) {
        super(name, mana, str, agi, dex, money, xp);
        // Default values - these will be set when the game starts and assigns lanes
        this.nexusRow = -1;
        this.nexusCol = -1;
        this.lane = "Unassigned";
    }

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
            setHp(this.level*100);// Reset HP to Max (Example Formula)
            setMana(this.level * 100); // Reset Mana to Max
        }
    }

    /**
     * A hero gets a reward when that hero or another hero in the party has defeated a monster
     */
    @Override
    public void getReward(int level)
    {
        this.gold += level * 500;
        System.out.println(name + " gained " + (level * 500) + " gold!");
        this.experience += level * 2;
        System.out.println(name + " gained " + level*2 + " EXP!");
        System.out.println();
        if (this.experience >= this.level * 10)
        {
            levelUp();
        }
    }

    @Override
    public void levelUp() {
        System.out.println(this.name + " has leveled up to " + this.level + " in Legends of Valors!");
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Lane: %s | Nexus: (%d, %d)", lane, nexusRow, nexusCol);
    }

    // Getters for Person 1 (Map) and Person 3 (Game Loop)
    public int getNexusRow() { return nexusRow; }
    public int getNexusCol() { return nexusCol; }
    public int getCurrentRow() { return currentRow; }
    public int getCurrentPos() { return currentCol; }
    public String getLane() { return lane; }
}