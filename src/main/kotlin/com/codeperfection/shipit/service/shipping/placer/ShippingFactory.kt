package com.codeperfection.shipit.service.shipping.placer

import com.codeperfection.shipit.entity.Product
import com.codeperfection.shipit.entity.ShippedItem
import com.codeperfection.shipit.entity.Shipping
import com.codeperfection.shipit.entity.Transporter
import com.codeperfection.shipit.exception.ShippingImpossibleException
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*

@Component
class ShippingFactory(
    private val knapsackPlacer: KnapsackPlacer,
    private val clock: Clock
) {

    fun create(name: String, products: List<Product>, transporter: Transporter): Shipping {
        val productToCount = calculateProductsToShip(products, transporter)
        return createShipping(name, transporter, productToCount)
    }

    private fun calculateProductsToShip(products: List<Product>, transporter: Transporter): Map<Product, Int> {
        if (products.isEmpty()) {
            throw ShippingImpossibleException(userId = transporter.userId, transporterId = transporter.id)
        }
        val knapsack = knapsackPlacer.place(createKnapsackItems(products), transporter.capacity)
        if (knapsack.isEmpty()) {
            throw ShippingImpossibleException(userId = transporter.userId, transporterId = transporter.id)
        }
        return productToCount(knapsack, products.associateBy { it.id })
    }

    private fun createKnapsackItems(products: List<Product>): Array<Knapsack.Item> = products.flatMap { product ->
        List(size = product.countInStock) {
            Knapsack.Item(parentId = product.id, volume = product.volume, price = product.price)
        }
    }.toTypedArray()

    private fun productToCount(knapsack: Knapsack, productsMap: Map<UUID, Product>): Map<Product, Int> =
        knapsack.items
            .map { productsMap.getValue(it.parentId) }
            .groupingBy { it }
            .eachCount()

    private fun createShipping(
        name: String,
        transporter: Transporter,
        productToCount: Map<Product, Int>
    ): Shipping {
        val shipping = Shipping(
            id = UUID.randomUUID(),
            userId = transporter.userId,
            name = name,
            createdAt = OffsetDateTime.now(clock),
            transporter = transporter,
            shippedItems = emptyList()
        )
        val shippedItems = productToCount.map { (product, quantity) ->
            ShippedItem(
                id = UUID.randomUUID(),
                quantity = quantity,
                product = product,
                shipping = shipping
            )
        }
        shipping.shippedItems = shippedItems

        return shipping
    }
}
