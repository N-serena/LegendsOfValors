package model.world;
import model.item.Item;
import model.market.Market;

import java.util.List;

public class MarketTile extends Tile {
    private Market market; // Each tile has a shop

    public MarketTile(List<Item> possibleItems) {
        super("Market");
        this.market = new Market(possibleItems);
    }

    public Market getMarket() { return market; }

    @Override
    public void enter() {
        System.out.println("You have arrived at a Market.");
    }
}