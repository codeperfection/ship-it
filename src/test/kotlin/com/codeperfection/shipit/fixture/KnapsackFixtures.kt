package com.codeperfection.shipit.fixture

import com.codeperfection.shipit.service.shipping.placer.Knapsack

val product1ItemFixture = Knapsack.Item(parentId = PRODUCT_ID_1, volume = 100, price = 12)
val product2ItemFixture = Knapsack.Item(parentId = PRODUCT_ID_2, volume = 70, price = 16)

val emptyKnapsack = Knapsack(TRANSPORTER_CAPACITY, 0, emptyList())
