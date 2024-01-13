package com.codeperfection.shipit.service.shipping.placer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class KnapsackPlacerTest {

    private val underTest = KnapsackPlacer()

    @Test
    fun `GIVEN empty item list, WHEN placing items, THEN empty knapsack is returned`() {
        val result = underTest.place(emptyArray<Knapsack.Item>(), 10)
        assertThat(result.isEmpty()).isTrue()
    }

    @Test
    fun `GIVEN item with higher volume than capacity, WHEN placing items, THEN empty knapsack is returned`() {
        val parentIdFixture = UUID.fromString("cd8ff02c-c5b0-49cb-b6d4-a310d12b46aa")
        val result = underTest.place(arrayOf(Knapsack.Item(parentIdFixture, 20, 20)), 10)
        assertThat(result.isEmpty()).isTrue()
    }

    @Test
    fun `GIVEN items which can be placed, WHEN placing items, THEN expected knapsack is returned`() {
        val parentIdFixture1 = UUID.fromString("cd8ff02c-c5b0-49cb-b6d4-a310d12b46aa")
        val item1 = Knapsack.Item(parentIdFixture1, 15, 17)
        val parentIdFixture2 = UUID.fromString("afc78ea1-f4ac-4142-8d88-5a1983f9e0e2")
        val item2 = Knapsack.Item(parentIdFixture2, 21, 33)
        val parentIdFixture3 = UUID.fromString("9df7fea5-53b4-4b36-b3c9-be6ae83ea30b")
        val item3 = Knapsack.Item(parentIdFixture3, 30, 45)
        val parentIdFixture4 = UUID.fromString("780acfce-b47f-40d2-9e3b-d33f977509bb")
        val item4 = Knapsack.Item(parentIdFixture4, 1, 1)

        val items = arrayOf(
            item1, item1, item1, item1,
            item2, item2,
            item3, item3, item3,
            item4
        )
        val capacity = 100
        val result = underTest.place(items, capacity)

        val expectedPlacedItems = listOf(item4, item3, item3, item2, item1)
        assertThat(result).isEqualTo(Knapsack(capacity, 141, expectedPlacedItems))
    }
}
