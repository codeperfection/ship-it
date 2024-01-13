package com.codeperfection.shipit.service.shipping.placer

import com.codeperfection.shipit.exception.InternalServerErrorException
import org.springframework.stereotype.Component
import java.util.*
import kotlin.math.max

data class Knapsack(val capacity: Int, val totalPrice: Int, val items: List<Item>) {

    data class Item(val parentId: UUID, val volume: Int, val price: Int)

    fun isEmpty() = items.isEmpty()
}

@Component
class KnapsackPlacer {

    fun place(items: Array<Knapsack.Item>, capacity: Int): Knapsack {
        val maxTotalPrices = calculateMaxTotalPrices(items, capacity)

        /**
         * After calculating max total possible prices for each combination of items and capacity
         * now we need to go backwards in order to retrieve actual items which make the calculated overall price.
         * In each step we need to consider item to be added to the sack or ignored,
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
                break;
            }

            // Skip if item doesn't help maximizing the price
            if (goalPriceToFill == maxTotalPrices[itemsSize - 1][goalCapacityToFill]) {
                /**
                 * If the goal price came from maxTotalPrices[ itemsSize - 1 ][ goalCapacityToFill ],
                 * then the last item was not included to maximize the price.
                 */
                continue;
            }

            // Store item
            val lastItem = items[itemsSize - 1]
            resultItems.add(lastItem)

            // Adjust goals
            goalPriceToFill -= lastItem.price
            goalCapacityToFill -= lastItem.volume

            if (goalPriceToFill < 0 || goalCapacityToFill < 0) {
                /**
                 * After readjusting goals, there are only to possibilities:
                 * 1. (goals are 0) We have found all the items which were used to calculate max total prices
                 * 2. (goals are greater than 0) We still need to run through the rest of the items,
                 *    to find the once which were used to calculate max total price
                 *
                 * So if we have reached this case when one of the goal amount is negative,
                 * it is a clear sign that our algorithm has bugs
                 */
                throw InternalServerErrorException("Something went wrong in the core algorithm")
            }
        }

        return Knapsack(capacity, maxTotalPrices[items.size][capacity], resultItems)
    }

    /**
     * Aim is to calculate combination of items, which doesn't exceed capacity and have maximum possible price sum.
     */
    private fun calculateMaxTotalPrices(items: Array<Knapsack.Item>, capacity: Int): Array<IntArray> {
        /**
         * initial state: empty total prices matrix
         * where
         *  - n is item's count
         *  - m is capacity
         *
         *     0 1 . . . m
         *   | - - - - - - |
         * 0 | 0 0 . . . 0 |
         * 1 | 0 0 . . . 0 |
         * . | . .         |
         * . | . .         |
         * . | . .         |
         * n | 0 0 . . . 0 |
         *
         * When there are no items under consideration (1st row) maximum prices are 0
         * When there is no capacity to place items (1st column) maximum prices are 0
         * We need to fill inner part of the matrix, based on the previous calculation
         */
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
}
