package com.codeperfection.shipit.dto.product

import com.codeperfection.shipit.entity.Product
import java.util.*

data class ProductDto(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val volume: Int,
    val price: Int,
    val countInStock: Int
) {
    companion object {
        fun fromEntity(product: Product) = ProductDto(
            id = product.id,
            userId = product.userId,
            name = product.name,
            volume = product.volume,
            price = product.price,
            countInStock = product.countInStock
        )
    }
}
