package model.market;

import model.item.Item;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Market class represents a shop in the game that offers a unique selection of items for sale.
 * Each time a Market instance is created, it generates a random assortment of items from the master list.
 ** @author Serena N
 * @version 1.0
 */
public class Market {
    private List<Item> itemsForSale;

    // Constructor takes the Master List of all game items
    public Market(List<Item> allGameItems) {
        this.itemsForSale = new ArrayList<>();
        generateUniqueInventory(allGameItems);
    }

    private void generateUniqueInventory(List<Item> allItems) {
        // 1. Create a temporary copy to shuffle (so we don't mess up the master list)
        List<Item> pool = new ArrayList<>(allItems);
        Collections.shuffle(pool);

        // 2. Determine Shop Size (e.g., Randomly 5 to 10 items)
        Random rand = new Random();
        int shopSize = 5 + rand.nextInt(6);

        // 3. Fill the shelves
        // Ensure we don't try to add more items than exist in the game
        int limit = Math.min(shopSize, pool.size());

        for (int i = 0; i < limit; i++) {
            this.itemsForSale.add(pool.get(i));
        }
    }

    public List<Item> getItems() {
        return itemsForSale;
    }
}