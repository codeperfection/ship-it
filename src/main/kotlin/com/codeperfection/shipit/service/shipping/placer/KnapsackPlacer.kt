package com.codeperfection.shipit.service.shipping.placer

import org.springframework.stereotype.Component
import java.util.*
import kotlin.math.max

data class Knapsack(val capacity: Int, val totalPrice: Int, val items: List<Item>) {

    data class Item(val parentId: UUID, val volume: Int, val price: Int)

    fun isEmpty(): Boolean = items.isEmpty()
}

@Component
class KnapsackPlacer {

    fun place(items: Array<Knapsack.Item>, capacity: Int): Knapsack {
        val maxTotalPrices = calculateMaxTotalPrices(items, capacity)
        return backtrackItemsUsedInKnapsack(items, capacity, maxTotalPrices)
    }

    /**
     * Calculate combination of items, which doesn't exceed capacity and have maximum possible price sum.
     */
    private fun calculateMaxTotalPrices(items: Array<Knapsack.Item>, capacity: Int): Array<IntArray> {
        val maxTotalPrices = Array(items.size + 1) { IntArray(capacity + 1) { 0 } }

        for (size in 1..items.size) {
            for (currentCapacity in 0..capacity) {
                maxTotalPrices[size][currentCapacity] = calculateMaxTotalPrice(
                    items = items,
                    size = size,
                    capacity = currentCapacity,
                    previousMaxTotalPrices = maxTotalPrices
                )
            }
        }
        return maxTotalPrices
    }

    private fun calculateMaxTotalPrice(
        items: Array<Knapsack.Item>,
        size: Int,
        capacity: Int,
        previousMaxTotalPrices: Array<IntArray>
    ): Int {
        val lastItem = items[size - 1]

        val withoutLastItem = previousMaxTotalPrices[size - 1][capacity]
        if (lastItem.volume > capacity) {
            // last item cannot be placed as it will exceed overall capacity
            return withoutLastItem
        }

        val capacityWithoutLastItem = capacity - lastItem.volume
        val withLastItem = lastItem.price + previousMaxTotalPrices[size - 1][capacityWithoutLastItem]
        return max(withoutLastItem, withLastItem)
    }

    private fun backtrackItemsUsedInKnapsack(
        items: Array<Knapsack.Item>,
        capacity: Int,
        maxTotalPrices: Array<IntArray>
    ): Knapsack {
        /**
         * After calculating max total possible prices for each combination of items and capacity
         * now we need to go backwards in order to retrieve actual items which make the calculated overall price.
         * In each step we need to consider item to be added to the knapsack or ignored,
         * based on whether the item was used in calculation of the goal max total price.
         * If item was involved in the max total price calculation
         * - we store item in the final item list
         * - adjust goal price and capacity to not consider attributes of the item
         * - move to next item
         */
        var goalPriceToFill = maxTotalPrices[items.size][capacity]
        var goalCapacityToFill = capacity

        val resultItems = mutableListOf<Knapsack.Item>()

        for (itemsSize in items.size downTo 1) {
            // Stop if we have collected all items summing up to the goal
            if (goalPriceToFill == 0) {
                break
            }

            /**
             * If the goal price came from maxTotalPrices[itemsSize - 1][ goalCapacityToFill ],
             * then the last item was not included to maximize the price.
             */
            if (goalPriceToFill == maxTotalPrices[itemsSize - 1][goalCapacityToFill]) {
                continue
            }

            val lastItem = items[itemsSize - 1]
            resultItems.add(lastItem)
            goalPriceToFill -= lastItem.price
            goalCapacityToFill -= lastItem.volume
        }

        return Knapsack(capacity, maxTotalPrices[items.size][capacity], resultItems)
    }
}
