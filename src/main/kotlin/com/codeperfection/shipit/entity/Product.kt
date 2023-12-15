package com.codeperfection.shipit.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.OffsetDateTime
import java.util.*

@Entity
data class Product(
    @Id
    val id: UUID,
    val userId: UUID,
    val name: String,
    val volume: Int,
    val price: Int,
    var countInStock: Int,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    var isActive: Boolean
)
