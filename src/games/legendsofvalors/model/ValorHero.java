package games.legendsofvalors.model;

import core.model.entity.Hero;

public class ValorHero extends Hero {

    // LOV Specific Attributes
    private int nexusRow;
    private int nexusCol;
    private String lane; // "Top", "Mid", "Bot"

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
    public void recall() {
        if (nexusRow != -1 && nexusCol != -1) {
            // NOTE: We will need to talk to the Board to move visually,
            // but for now, we reset the internal state.
            this.hp = this.level * 100; // Reset HP to Max (Example Formula)
            this.mana = this.level * 100; // Reset Mana to Max
            System.out.println(this.name + " has been recalled to the " + this.lane + " Nexus!");
        }
    }

    @Override
    public void levelUp() {
        // Additional LoV-specific level up logic can go here
        System.out.println(this.name + " has leveled up to " + this.level + " in Legends of Valors!");
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Lane: %s | Nexus: (%d, %d)", lane, nexusRow, nexusCol);
    }

    // Getters for Person 1 (Map) and Person 3 (Game Loop)
    public int getNexusRow() { return nexusRow; }
    public int getNexusCol() { return nexusCol; }
    public String getLane() { return lane; }
}