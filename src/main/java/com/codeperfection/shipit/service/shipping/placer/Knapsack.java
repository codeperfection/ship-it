package com.codeperfection.shipit.service.shipping.placer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

/**
 * Optimally placed knapsack. The total volume of all the items is less than or equal to the capacity of the knapsack,
 * while the total price is as large as possible.
 */
@AllArgsConstructor
@EqualsAndHashCode
@Getter
class Knapsack implements ItemsStorage {

    private int capacity;

    private int totalPrice;

    private List<Item> items;
}
