package games.commoncontrollers;

import core.interfaces.HeroObserver;
import core.interfaces.HeroEventNotifier;

import java.util.ArrayList;
import java.util.List;

/**
 * A concerte publisher class, following the observer design pattern, to implement the publisher interface
 * * @author Chris Mary Benson.
 * @version 1.0
 */

public class PartyController implements HeroEventNotifier {
    //acts as a publisher in order to implement the observer design pattern
    //to alert other heroes (who are the subscribers), whenever a hero defeats an enemy
    //because all heroes gain rewards when a monster is defeated

    private final List<HeroObserver> observers = new ArrayList<>();

    /**
     * A method to register an observer
     * @param o the hero who wants to act as an observer
     */
    @Override
    public void addObserver(HeroObserver o)
    {
        observers.add(o);
    }

    /**
     * A method to remove an observer
     * @param o the hero observer
     */
    @Override
    public void removeObserver(HeroObserver o)
    {
        //can be used when a hero has fainted in the current round
        observers.remove(o);
    }

    /**
     * A method to notify the oberservers of an update
     * @param level the level of the monster defeated
     */
    public void notifyObservers(int level)
    {
        for (HeroObserver observer:  observers)
        {
            observer.getReward(level); //each hero gets the reward
        }
    }

    //helper
    @Override
    public void defeatedMonster(int level)
    {
        notifyObservers(level);
    }
}
