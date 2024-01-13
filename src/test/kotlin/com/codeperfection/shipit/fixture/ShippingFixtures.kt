package com.codeperfection.shipit.fixture

import com.codeperfection.shipit.dto.shipping.CreateShippingDto
import com.codeperfection.shipit.dto.shipping.ShippedItemDto
import com.codeperfection.shipit.dto.shipping.ShippingDto
import com.codeperfection.shipit.entity.ShippedItem
import com.codeperfection.shipit.entity.Shipping
import java.time.OffsetDateTime
import java.util.UUID

val SHIPPED_ITEM_ID_1: UUID = UUID.fromString("f11a5be8-acdf-4fd0-a329-9190091392d2")
const val SHIPPED_ITEM_QUANTITY_1 = 2
fun createShippedItemFixture1() = ShippedItem(
    id = SHIPPED_ITEM_ID_1,
    product = createProductFixture1(),
    quantity = SHIPPED_ITEM_QUANTITY_1,
    shipping = null
)
val shippedItemDtoFixture1 = ShippedItemDto(
    id = SHIPPED_ITEM_ID_1,
    product = productDtoFixture1,
    quantity = SHIPPED_ITEM_QUANTITY_1
)

val SHIPPED_ITEM_ID_2: UUID = UUID.fromString("c97a79bb-af61-4f68-b646-9709f904b3a2")
const val SHIPPED_ITEM_QUANTITY_2 = 4
fun createShippedItemFixture2() = ShippedItem(
    id = SHIPPED_ITEM_ID_2,
    product = createProductFixture2(),
    quantity = SHIPPED_ITEM_QUANTITY_2,
    shipping = null
)
val shippedItemDtoFixture2 = ShippedItemDto(
    id = SHIPPED_ITEM_ID_2,
    product = productDtoFixture2,
    quantity = SHIPPED_ITEM_QUANTITY_2
)

val SHIPPING_ID: UUID = UUID.fromString("62b6772b-384f-4e4d-861f-6e126ebc30b5")
const val SHIPPING_NAME = "Shipping1"
val SHIPPING_CREATED_AT: OffsetDateTime = OffsetDateTime.parse("2024-01-01T11:00Z")

fun createShippingFixture(): Shipping {
    val shippedItemFixture1 = createShippedItemFixture1()
    val shippedItemFixture2 = createShippedItemFixture2()
    val result =  Shipping(
        id = SHIPPING_ID,
        userId = USER_ID,
        name = SHIPPING_NAME,
        createdAt = SHIPPING_CREATED_AT,
        transporter = createTransporterFixture(),
        shippedItems = listOf(shippedItemFixture1, shippedItemFixture2)
    )
    shippedItemFixture1.shipping = result
    shippedItemFixture2.shipping = result
    return result
}

val createShippingDtoFixture = CreateShippingDto(
    name = SHIPPING_NAME,
    transporterId = TRANSPORTER_ID
)

val shippingDtoFixture = ShippingDto(
    id = SHIPPING_ID,
    name = SHIPPING_NAME,
    userId = USER_ID,
    createdAt = SHIPPING_CREATED_AT,
    transporter = transporterDtoFixture,
    shippedItems = listOf(
        shippedItemDtoFixture1,
        shippedItemDtoFixture2
    )
)
