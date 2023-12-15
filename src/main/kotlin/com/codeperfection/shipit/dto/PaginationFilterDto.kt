package com.codeperfection.shipit.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class PaginationFilterDto(
    @field:Min(0)
    val page: Int = 0,
    @field:Min(1)
    @field:Max(100)
    val size: Int = 100
)
