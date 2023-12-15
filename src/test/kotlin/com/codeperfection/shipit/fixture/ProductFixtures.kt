package com.codeperfection.shipit.fixture

import com.codeperfection.shipit.dto.product.CreateProductDto
import com.codeperfection.shipit.dto.product.ProductDto
import com.codeperfection.shipit.entity.Product
import java.time.OffsetDateTime
import java.util.*

val PRODUCT_ID: UUID = UUID.fromString("5be80385-205f-4c30-873b-a1c5ed59985a")
const val PRODUCT_NAME = "Product1"
const val PRODUCT_VOLUME = 100
const val PRODUCT_PRICE = 12
const val PRODUCT_COUNT_IN_STOCK = 7
val PRODUCT_CREATION_DATE: OffsetDateTime = OffsetDateTime.parse("2023-01-12T19:30:46.954Z")

fun createProductFixture() = Product(
    id = PRODUCT_ID,
    userId = USER_ID,
    name = PRODUCT_NAME,
    volume = PRODUCT_VOLUME,
    price = PRODUCT_PRICE,
    countInStock = PRODUCT_COUNT_IN_STOCK,
    createdAt = PRODUCT_CREATION_DATE,
    updatedAt = PRODUCT_CREATION_DATE,
    isActive = true
)

val createProductDtoFixture = CreateProductDto(
    name = PRODUCT_NAME,
    volume = PRODUCT_VOLUME,
    price = PRODUCT_PRICE,
    countInStock = PRODUCT_COUNT_IN_STOCK
)

val productDtoFixture = ProductDto(
    id = PRODUCT_ID,
    userId = USER_ID,
    name = PRODUCT_NAME,
    volume = PRODUCT_VOLUME,
    price = PRODUCT_PRICE,
    countInStock = PRODUCT_COUNT_IN_STOCK
)
