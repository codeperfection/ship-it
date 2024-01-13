package com.codeperfection.shipit.repository

import com.codeperfection.shipit.entity.Shipping
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ShippingRepository : JpaRepository<Shipping, UUID> {

    fun findByUserId(userId: UUID, pageable: Pageable): Page<Shipping>

    fun findByIdAndUserId(id: UUID, userId: UUID): Shipping?
}
