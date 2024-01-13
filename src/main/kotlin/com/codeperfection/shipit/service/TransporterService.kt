package com.codeperfection.shipit.service

import com.codeperfection.shipit.dto.common.PageDto
import com.codeperfection.shipit.dto.common.PaginationFilterDto
import com.codeperfection.shipit.dto.transporter.CreateTransporterDto
import com.codeperfection.shipit.dto.transporter.TransporterDto
import com.codeperfection.shipit.entity.Transporter
import com.codeperfection.shipit.exception.clienterror.NotFoundException
import com.codeperfection.shipit.repository.TransporterRepository
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*

@Component
class TransporterProvider(private val transporterRepository: TransporterRepository) {

    fun getTransporter(userId: UUID, transporterId: UUID) = transporterRepository
        .findByIdAndUserIdAndIsActiveTrue(transporterId, userId)
        ?: throw NotFoundException(transporterId, userId)
}

@Service
class TransporterService(
    private val transporterRepository: TransporterRepository,
    private val transporterProvider: TransporterProvider,
    private val clock: Clock
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    @PreAuthorize("hasAuthority('SCOPE_shipit:write')")
    fun createTransporter(userId: UUID, createTransporterDto: CreateTransporterDto): TransporterDto {
        val now = OffsetDateTime.now(clock)
        val id = UUID.randomUUID()
        val savedTransporter = transporterRepository.save(
            Transporter(
                id = id,
                userId = userId,
                name = createTransporterDto.name,
                capacity = createTransporterDto.capacity,
                createdAt = now,
                updatedAt = now,
                isActive = true
            )
        )

        logger.info("Created transporter '${savedTransporter.name}' with ID $id for user with ID $userId")
        return TransporterDto.fromEntity(savedTransporter)
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCOPE_shipit:read')")
    fun getTransporters(userId: UUID, paginationFilterDto: PaginationFilterDto): PageDto<TransporterDto> {
        val transportersPage = transporterRepository
            .findByUserIdAndIsActiveTrue(userId, paginationFilterDto.toPageable())

        return PageDto(
            totalElements = transportersPage.totalElements,
            totalPages = transportersPage.totalPages,
            elements = transportersPage.toList().map(TransporterDto::fromEntity)
        )
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCOPE_shipit:read')")
    fun getTransporter(userId: UUID, transporterId: UUID) = TransporterDto
        .fromEntity(transporterProvider.getTransporter(userId, transporterId))

    @Transactional
    @PreAuthorize("hasAuthority('SCOPE_shipit:write')")
    fun deleteTransporter(userId: UUID, transporterId: UUID) {
        val transporter = transporterRepository.findByIdAndUserIdAndIsActiveTrue(transporterId, userId)
            ?: throw NotFoundException(transporterId, userId)
        transporter.isActive = false
        transporterRepository.save(transporter)
        logger.info("Deleted transporter '${transporter.name}' with ID ${transporter.id} for user with ID $userId")
    }
}
