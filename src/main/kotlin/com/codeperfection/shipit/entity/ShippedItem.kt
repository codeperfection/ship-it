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

    // Default implementation takes into account whole shipping object,
    // which creates stack-overflow as shipping contains this too
    override fun toString(): String {
        return "ShippedItem(id=$id, quantity=$quantity, product=$product, shippingId=${shipping?.id})"
    }

    // Default implementation takes into account whole shipping object,
    // which creates stack-overflow as shipping contains this too
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as ShippedItem

        return id == other.id && quantity == other.quantity && product == other.product &&
                shipping?.id == other.shipping?.id
    }

    // Default implementation takes into account whole shipping object,
    // which creates stack-overflow as shipping contains this too
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + quantity
        result = 31 * result + product.hashCode()
        result = 31 * result + shipping?.id.hashCode()
        return result
    }
}
