package com.codeperfection.shipit.service.shipping

import com.codeperfection.shipit.dto.common.PageDto
import com.codeperfection.shipit.dto.common.PaginationFilterDto
import com.codeperfection.shipit.dto.shipping.ShippingDto
import com.codeperfection.shipit.exception.clienterror.NotFoundException
import com.codeperfection.shipit.repository.ShippingRepository
import com.codeperfection.shipit.service.AuthorizationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ShippingManagementService(
    private val shippingRepository: ShippingRepository,
    private val authorizationService: AuthorizationService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getShippings(userId: UUID, paginationFilterDto: PaginationFilterDto): PageDto<ShippingDto> {
        authorizationService.checkReadAccess(userId)
        val shippingsPage = shippingRepository.findByUserId(userId, paginationFilterDto.toPageable())
        return PageDto(
            totalElements = shippingsPage.totalElements,
            totalPages = shippingsPage.totalPages,
            elements = shippingsPage.toList().map(ShippingDto::fromEntity)
        )
    }

    @Transactional(readOnly = true)
    fun getShipping(userId: UUID, shippingId: UUID): ShippingDto {
        authorizationService.checkReadAccess(userId)
        return shippingRepository
            .findByIdAndUserId(shippingId, userId)?.let(ShippingDto::fromEntity)
            ?: throw NotFoundException(shippingId, userId)
    }

    @Transactional
    fun deleteShipping(userId: UUID, shippingId: UUID) {
        authorizationService.checkWriteAccess(userId)
        val shipping = shippingRepository.findByIdAndUserId(shippingId, userId)
            ?: throw NotFoundException(shippingId, userId)
        shippingRepository.delete(shipping)
        logger.info("Deleted shipping '${shipping.name}' with ID ${shipping.id} for user with ID $userId")
    }
}
