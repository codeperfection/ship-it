package com.codeperfection.shipit.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
data class Shipping(
    @Id
    val id: UUID,
    val userId: UUID,
    val name: String,
    val createdAt: OffsetDateTime,
    @ManyToOne
    @JoinColumn(name = "transporter_id")
    val transporter: Transporter,
    @OneToMany(mappedBy = "shipping", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var shippedItems: List<ShippedItem>
)
