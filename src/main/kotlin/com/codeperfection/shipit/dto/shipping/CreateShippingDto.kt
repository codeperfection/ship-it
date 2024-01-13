package com.codeperfection.shipit.dto.shipping

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateShippingDto(
    @field:NotBlank
    @field:Size(max = 256)
    val name: String,
    val transporterId: UUID
)
