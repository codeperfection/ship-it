package com.codeperfection.shipit.fixture

import com.codeperfection.shipit.dto.product.CreateProductDto
import com.codeperfection.shipit.dto.product.ProductDto
import com.codeperfection.shipit.entity.Product
import java.time.OffsetDateTime
import java.util.*

val PRODUCT_CREATION_DATE: OffsetDateTime = OffsetDateTime.parse("2023-01-12T19:30:46.954Z")

val PRODUCT_ID_1: UUID = UUID.fromString("5be80385-205f-4c30-873b-a1c5ed59985a")
const val PRODUCT_NAME_1 = "Product1"
const val PRODUCT_VOLUME_1 = 100
const val PRODUCT_PRICE_1 = 12
const val PRODUCT_COUNT_IN_STOCK_1 = 7

fun createProductFixture1() = Product(
    id = PRODUCT_ID_1,
    userId = USER_ID,
    name = PRODUCT_NAME_1,
    volume = PRODUCT_VOLUME_1,
    price = PRODUCT_PRICE_1,
    countInStock = PRODUCT_COUNT_IN_STOCK_1,
    createdAt = PRODUCT_CREATION_DATE,
    updatedAt = PRODUCT_CREATION_DATE,
    isActive = true
)

val productDtoFixture1 = ProductDto(
    id = PRODUCT_ID_1,
    userId = USER_ID,
    name = PRODUCT_NAME_1,
    volume = PRODUCT_VOLUME_1,
    price = PRODUCT_PRICE_1,
    countInStock = PRODUCT_COUNT_IN_STOCK_1
)

val createProductDtoFixture1 = CreateProductDto(
    name = PRODUCT_NAME_1,
    volume = PRODUCT_VOLUME_1,
    price = PRODUCT_PRICE_1,
    countInStock = PRODUCT_COUNT_IN_STOCK_1
)

val PRODUCT_ID_2: UUID = UUID.fromString("a6cde4e6-1cfc-4e99-87a4-75a54069fe99")
const val PRODUCT_NAME_2 = "Product2"
const val PRODUCT_VOLUME_2 = 70
const val PRODUCT_PRICE_2 = 16
const val PRODUCT_COUNT_IN_STOCK_2 = 9

fun createProductFixture2() = Product(
    id = PRODUCT_ID_2,
    userId = USER_ID,
    name = PRODUCT_NAME_2,
    volume = PRODUCT_VOLUME_2,
    price = PRODUCT_PRICE_2,
    countInStock = PRODUCT_COUNT_IN_STOCK_2,
    createdAt = PRODUCT_CREATION_DATE,
    updatedAt = PRODUCT_CREATION_DATE,
    isActive = true
)

val productDtoFixture2 = ProductDto(
    id = PRODUCT_ID_2,
    userId = USER_ID,
    name = PRODUCT_NAME_2,
    volume = PRODUCT_VOLUME_2,
    price = PRODUCT_PRICE_2,
    countInStock = PRODUCT_COUNT_IN_STOCK_2
)
