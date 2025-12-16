package core.interfaces;

/**
 * A subscriber interface, following the observer design pattern, to receive updates triggered by hero events
 * * @author Chris Mary Benson.
 * @version 1.0
 */
public interface HeroObserver {
    public void getReward(int monsterLevel);
}
