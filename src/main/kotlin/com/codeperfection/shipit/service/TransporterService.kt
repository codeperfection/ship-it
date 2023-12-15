package com.codeperfection.shipit.service

import com.codeperfection.shipit.dto.PageDto
import com.codeperfection.shipit.dto.PaginationFilterDto
import com.codeperfection.shipit.dto.transporter.CreateTransporterDto
import com.codeperfection.shipit.dto.transporter.TransporterDto
import com.codeperfection.shipit.entity.Transporter
import com.codeperfection.shipit.exception.clienterror.NotFoundException
import com.codeperfection.shipit.repository.TransporterRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*

@Service
class TransporterService(
    private val transporterRepository: TransporterRepository,
    private val clock: Clock
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    @PreAuthorize("hasAuthority('SCOPE_shipit:write')")
    fun createTransporter(userId: UUID, createTransporterDto: CreateTransporterDto): TransporterDto {
        logger.info("Creating transporter ${createTransporterDto.name} for user $userId")
        val now = OffsetDateTime.now(clock)
        val savedTransporter = transporterRepository.save(
            Transporter(
                id = UUID.randomUUID(),
                userId = userId,
                name = createTransporterDto.name,
                capacity = createTransporterDto.capacity,
                createdAt = now,
                updatedAt = now,
                isActive = true
            )
        )

        return mapToDto(savedTransporter)
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCOPE_shipit:read')")
    fun getTransporters(userId: UUID, paginationFilterDto: PaginationFilterDto): PageDto<TransporterDto> {
        val transportersPage = transporterRepository.findByUserIdAndIsActiveTrue(
            userId = userId,
            pageable = PageRequest.of(paginationFilterDto.page, paginationFilterDto.size, Sort.by("createdAt"))
        )

        return PageDto(
            totalElements = transportersPage.totalElements,
            totalPages = transportersPage.totalPages,
            elements = transportersPage.toList().map { mapToDto(it) }
        )
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCOPE_shipit:read')")
    fun getTransporter(userId: UUID, transporterId: UUID): TransporterDto {
        val transporter = transporterRepository.findByIdAndUserIdAndIsActiveTrue(transporterId, userId)
            ?: throw NotFoundException(transporterId)
        return mapToDto(transporter)
    }

    @Transactional
    @PreAuthorize("hasAuthority('SCOPE_shipit:write')")
    fun deleteTransporter(userId: UUID, transporterId: UUID) {
        val transporter = transporterRepository.findByIdAndUserIdAndIsActiveTrue(transporterId, userId)
            ?: throw NotFoundException(transporterId)
        transporter.isActive = false
        transporterRepository.save(transporter)
    }

    private fun mapToDto(transporter: Transporter) = TransporterDto(
        id = transporter.id,
        userId = transporter.userId,
        name = transporter.name,
        capacity = transporter.capacity
    )
}
