package com.codeperfection.shipit.dto.shipping

import com.codeperfection.shipit.dto.transporter.TransporterDto
import com.codeperfection.shipit.entity.Shipping
import java.time.OffsetDateTime
import java.util.*

data class ShippingDto(
    val id: UUID,
    val name: String,
    val userId: UUID,
    val createdAt: OffsetDateTime,
    val transporter: TransporterDto,
    val shippedItems: List<ShippedItemDto>
) {

    companion object {

        fun fromEntity(shipping: Shipping) = ShippingDto(
            id = shipping.id,
            name = shipping.name,
            createdAt = shipping.createdAt,
            userId = shipping.userId,
            transporter = TransporterDto.fromEntity(shipping.transporter),
            shippedItems = shipping.shippedItems.map(ShippedItemDto::fromEntity)
        )
    }
}
