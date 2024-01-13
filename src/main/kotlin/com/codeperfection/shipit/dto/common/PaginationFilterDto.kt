package com.codeperfection.shipit.dto.common

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

data class PaginationFilterDto(
    @field:Min(0)
    val page: Int = 0,
    @field:Min(1)
    @field:Max(100)
    val size: Int = 100
) {
    fun toPageable(): Pageable = PageRequest.of(page, size, Sort.by("createdAt"))
}
