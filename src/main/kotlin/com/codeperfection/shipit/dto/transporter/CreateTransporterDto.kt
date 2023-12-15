package com.codeperfection.shipit.dto.transporter

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateTransporterDto(
    @field:NotBlank
    @field:Size(max = 256)
    val name: String,
    @field:Min(1)
    val capacity: Int
)
