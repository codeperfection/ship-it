package com.codeperfection.shipit.service.shipping.placer;

import com.codeperfection.shipit.entity.Product;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class KnapsackPlacerTest {

    private final KnapsackPlacer knapsackPlacer = new KnapsackPlacer();

    @Test
    public void place_IfEmptyItems_ReturnsEmptyKnapsack() {
        int capacity = 7;
        final var knapsack = knapsackPlacer.place(new Item[]{}, capacity);
        assertThat(knapsack).isEqualTo(new Knapsack(capacity, 0, Collections.emptyList()));
    }

    @Test
    public void place_IfTooLargeItems_ReturnsEmptyKnapsack() {
        int capacity = 7;
        final var largeItem = new Item(mock(Product.class), 10, 10);
        final var knapsack = knapsackPlacer.place(new Item[]{largeItem}, capacity);
        assertThat(knapsack).isEqualTo(new Knapsack(capacity, 0, Collections.emptyList()));
    }

    @Test
    public void place_IfNormalItems_ReturnsOptimallyPlacedKnapsack() {
        final var item1 = new Item(mock(Product.class), 15, 17);
        final var item2 = new Item(mock(Product.class), 21, 33);
        final var item3 = new Item(mock(Product.class), 30, 45);
        final var item4 = new Item(mock(Product.class), 1, 1);
        Item[] items = new Item[]{
                item1, item1, item1, item1,
                item2, item2,
                item3, item3, item3,
                item4
        };
        int capacity = 100;
        final var knapsack = knapsackPlacer.place(items, capacity);
        var expectedItems = List.of(item4, item3, item3, item2, item1);
        assertThat(knapsack).isEqualTo(new Knapsack(capacity, 141, expectedItems));
    }
}
