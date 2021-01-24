package com.codeperfection.shipit.service.shipping.placer;

import lombok.Value;

import java.util.List;

/**
 * Optimally placed knapsack. The total volume of all the items is less than or equal to the capacity of the knapsack,
 * while the total price is as large as possible.
 */
@Value
public class Knapsack {

    int capacity;

    int totalPrice;

    List<Item> items;
}
