package model;

import model.entity.Hero;
import java.util.ArrayList;
import java.util.List;

/**
 * Party class to manage a group of Hero entities.
 ** @author Serena N
 * @version 1.0
 */
public class Party {
    private List<Hero> heroes;

    // We will track the party's location on the grid here (for Phase 3)
    private int x;
    private int y;

    public Party() {
        this.heroes = new ArrayList<>();
        this.x = 0; // Default starting position
        this.y = 0;
    }

    public void addHero(Hero h) {
        heroes.add(h);
    }

    // Checks if every single hero in the party has 0 HP 
    public boolean isPartyFainted() {
        for (Hero h : heroes) {
            // If even one hero is still standing, the party is not fainted
            if (!h.isFainted()) {
                return false;
            }
        }
        // If the loop finishes, everyone is down
        return true;
    }

    public void replaceHero(int index, Hero newHero) {
        if (index >= 0 && index < heroes.size()) {
            heroes.set(index, newHero);
        }
    }

    public List<Hero> getHeroes() {
        return heroes;
    }

    public Hero getHero(int index) {
        return heroes.get(index);
    }

    public int getSize() {
        return heroes.size();
    }

    // Location Setters/Getters for the Board later
    public int getX() { return x; }
    public int getY() { return y; }
    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }
}