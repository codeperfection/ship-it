package com.codeperfection.shipit.dto

data class PageDto<T>(
    val totalElements: Long,
    val totalPages: Int,
    val elements: List<T>
)
