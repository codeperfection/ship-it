package com.codeperfection.shipit.dto.product

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateProductDto(
    @field:NotBlank
    @field:Size(max = 256)
    val name: String,
    @field:Min(1)
    val volume: Int,
    @field:Min(1)
    val price: Int,
    @field:Min(0)
    val countInStock: Int
)
