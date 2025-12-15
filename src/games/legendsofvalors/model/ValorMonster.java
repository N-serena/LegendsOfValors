package games.legendsofvalors.model;

import core.model.entity.Monster;

/** A core class for the valor monster, extends the Monster class
 ** @version 1.0
 */
public class ValorMonster extends Monster {

    private String lane;

    // Constructor chains to the core Monster
    public ValorMonster(String name, int level, int damage, int defense, int dodgeChance, String lane) {
        super(name, level, damage, defense, dodgeChance);
        this.lane = lane;
    }

    // Copy Constructor (Needed for Spawning new copies from the template list)
    public ValorMonster(Monster template, String lane) {
        super(template.getName(), template.getLevel(), template.getBaseDamage(), template.getDefense(), template.getDodgeChance());
        this.lane = lane;
    }

    public String getLane() {
        return lane;
    }
}