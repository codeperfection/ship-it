package com.codeperfection.shipit.placer;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KnapsackPlacer {

    public Knapsack place(Item[] items, int capacity) {
        int[][] maxTotalPrices = calculateMaxTotalPrices(items, capacity);
        int maxTotalPrice = maxTotalPrices[items.length][capacity];
        int currentCapacity = capacity;
        List<Item> optimalPlacementItems = new ArrayList<>();

        for (int itemsSize = items.length; itemsSize > 0 && maxTotalPrice > 0; itemsSize--) {
            // If the max total price came from maxTotalPrices[itemsSize - 1][currentCapacity], then the last item
            // was not included to maximize the price. Otherwise the last item was included and we should add it to
            // the final result.
            if (maxTotalPrice != maxTotalPrices[itemsSize - 1][currentCapacity]) {
                final var lastItem = items[itemsSize - 1];
                optimalPlacementItems.add(lastItem);
                maxTotalPrice -= lastItem.getPrice();
                currentCapacity -= lastItem.getVolume();
            }
        }
        return new Knapsack(capacity, maxTotalPrices[items.length][capacity], optimalPlacementItems);
    }

    private int[][] calculateMaxTotalPrices(Item[] items, int capacity) {
        int[][] maxTotalPrices = new int[items.length + 1][capacity + 1];
        for (int currentCapacity = 0; currentCapacity <= capacity; currentCapacity++) {
            maxTotalPrices[0][currentCapacity] = 0;
        }

        for (int itemsSize = 1; itemsSize <= items.length; itemsSize++) {
            for (int currentCapacity = 0; currentCapacity <= capacity; currentCapacity++) {
                int lastItemIndex = itemsSize - 1;
                final var lastItem = items[lastItemIndex];
                int withoutLastItem = maxTotalPrices[lastItemIndex][currentCapacity];
                if (lastItem.getVolume() > currentCapacity) {
                    maxTotalPrices[itemsSize][currentCapacity] = withoutLastItem;
                } else {
                    int capacityWithoutLastItem = currentCapacity - lastItem.getVolume();
                    int withLastItem = lastItem.getPrice() + maxTotalPrices[lastItemIndex][capacityWithoutLastItem];
                    maxTotalPrices[itemsSize][currentCapacity] = Math.max(withoutLastItem, withLastItem);
                }
            }
        }
        return maxTotalPrices;
    }
}
