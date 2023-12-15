package com.codeperfection.shipit.dto.product

import jakarta.validation.constraints.Min

data class UpdateProductDto(
    @field:Min(0)
    val countInStock: Int
)
