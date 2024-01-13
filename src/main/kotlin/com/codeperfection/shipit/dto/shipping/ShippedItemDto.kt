package com.codeperfection.shipit.dto.shipping

import com.codeperfection.shipit.dto.product.ProductDto
import com.codeperfection.shipit.entity.ShippedItem
import java.util.UUID

data class ShippedItemDto(
    val id: UUID,
    val product: ProductDto,
    val quantity: Int
) {

    companion object {

        fun fromEntity(shippedItem: ShippedItem) = ShippedItemDto(
            id = shippedItem.id,
            product = ProductDto.fromEntity(shippedItem.product),
            quantity = shippedItem.quantity
        )
    }
}
