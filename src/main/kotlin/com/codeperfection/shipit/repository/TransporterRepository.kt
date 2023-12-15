package com.codeperfection.shipit.repository

import com.codeperfection.shipit.entity.Transporter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface TransporterRepository : JpaRepository<Transporter, UUID> {

    fun findByUserIdAndIsActiveTrue(userId: UUID, pageable: Pageable): Page<Transporter>

    fun findByIdAndUserIdAndIsActiveTrue(transporterId: UUID, userId: UUID): Transporter?
}
