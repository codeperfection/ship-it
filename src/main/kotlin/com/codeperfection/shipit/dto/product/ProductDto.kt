package com.codeperfection.shipit.dto.product

import java.util.*

data class ProductDto(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val volume: Int,
    val price: Int,
    val countInStock: Int
)
