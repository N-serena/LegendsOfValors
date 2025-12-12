package core.interfaces;

/**
 * A publisher interface, following the observer design pattern, to alert other heroes of any changes/updates
 * * @author Chris Mary Benson.
 * @version 1.0
 */

public interface HeroEventNotifier {
    public void addObserver(HeroObserver o);
    public void removeObserver(HeroObserver o);
    public void defeatedMonster(int level);
}
