package com.codeperfection.shipit.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.util.*

@Entity
data class ShippedItem(
    @Id
    val id: UUID,
    val quantity: Int,
    @ManyToOne
    @JoinColumn(name = "product_id")
    val product: Product,
    @ManyToOne
    @JoinColumn(name = "shipping_id")
    var shipping: Shipping?
) {

    // Default implementation of toString, equals and hashCode takes into account all fields including "shipping",
    // which creates circular reference as shipping contains this too.
    override fun toString(): String =
        "ShippedItem(id=$id, quantity=$quantity)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShippedItem

        return id == other.id
    }

    override fun hashCode(): Int =
        id.hashCode()
}
