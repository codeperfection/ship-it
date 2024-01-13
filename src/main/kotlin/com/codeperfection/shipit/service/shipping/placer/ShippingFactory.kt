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
            throw ShippingImpossibleException(transporter)
        }
        val knapsack = knapsackPlacer.place(items(products), transporter.capacity)
        if (knapsack.isEmpty()) {
            throw ShippingImpossibleException(transporter)
        }
        return productToCount(knapsack, productsMap(products))
    }

    private fun items(products: List<Product>) = products.map {
        Knapsack.Item(parentId = it.id, volume = it.volume, price = it.price)
    }.toTypedArray()

    private fun productsMap(products: List<Product>) = products.associateBy { it.id }

    private fun productToCount(knapsack: Knapsack, productsMap: Map<UUID, Product>): Map<Product, Int> {
        val result = mutableMapOf<Product, Int>()
        knapsack.items.forEach {
            val product = productsMap[it.parentId]!!
            result[product] = result.getOrDefault(product, 0) + 1
        }
        return result
    }

    private fun createShipping(
        name: String,
        transporter: Transporter,
        productToCount: Map<Product, Int>
    ): Shipping {
        val emptyShipping = Shipping(
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
                shipping = emptyShipping
            )
        }

        return emptyShipping.copy(shippedItems = shippedItems)
    }
}
