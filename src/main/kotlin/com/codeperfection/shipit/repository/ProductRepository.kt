package com.codeperfection.shipit.repository

import com.codeperfection.shipit.entity.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ProductRepository : JpaRepository<Product, UUID> {

    fun findByUserIdAndIsActiveTrue(userId: UUID, pageable: Pageable): Page<Product>

    fun findAllByUserIdAndIsActiveTrue(userId: UUID): List<Product>

    fun findByIdAndUserIdAndIsActiveTrue(productId: UUID, userId: UUID): Product?
}
