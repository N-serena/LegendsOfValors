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

    @Override
    public void addObserver(HeroObserver o)
    {
        observers.add(o);
    }

    @Override
    public void removeObserver(HeroObserver o)
    {
        //can be used when a hero has fainted in the current round
        observers.remove(o);
    }

    public void notifyObservers(int level)
    {
        for (HeroObserver observer:  observers)
        {
            observer.getReward(level); //each hero gets the reward
        }
    }

    @Override
    public void defeatedMonster(int level)
    {
        notifyObservers(level);
    }
}
