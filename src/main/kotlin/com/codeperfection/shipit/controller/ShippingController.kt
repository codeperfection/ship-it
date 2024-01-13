package com.codeperfection.shipit.controller

import com.codeperfection.shipit.dto.common.PageDto
import com.codeperfection.shipit.dto.common.PaginationFilterDto
import com.codeperfection.shipit.dto.shipping.CreateShippingDto
import com.codeperfection.shipit.dto.shipping.ShippingDto
import com.codeperfection.shipit.exception.ShippingImpossibleException
import com.codeperfection.shipit.exception.dto.ApiError
import com.codeperfection.shipit.exception.dto.ErrorType
import com.codeperfection.shipit.service.shipping.ShippingManagementService
import com.codeperfection.shipit.service.shipping.placer.ShippingPlacementService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/api/v1/users/{userId}/shippings")
class ShippingController(
    private val shippingPlacementService: ShippingPlacementService,
    private val shippingManagementService: ShippingManagementService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun createShipping(
        @PathVariable userId: UUID,
        @Valid @RequestBody createShippingDto: CreateShippingDto
    ): ResponseEntity<ShippingDto> {
        val shippingDto = shippingPlacementService.createShipping(userId, createShippingDto)
        return ResponseEntity.created(location(shippingDto)).body(shippingDto)
    }

    @GetMapping
    fun getShippings(
        @PathVariable userId: UUID,
        @Valid paginationFilterDto: PaginationFilterDto
    ): PageDto<ShippingDto> = shippingManagementService.getShippings(userId, paginationFilterDto)

    @GetMapping("/{shippingId}")
    fun getShipping(@PathVariable userId: UUID, @PathVariable shippingId: UUID) =
        shippingManagementService.getShipping(userId, shippingId)

    @DeleteMapping("/{shippingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteShipping(@PathVariable userId: UUID, @PathVariable shippingId: UUID) =
        shippingManagementService.deleteShipping(userId, shippingId)

    @ExceptionHandler(ShippingImpossibleException::class)
    fun shippingImpossibleException(e: ShippingImpossibleException): ResponseEntity<ApiError> {
        logger.warn(e.message)
        return ResponseEntity.status(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
            .body(
                ApiError(
                    status = HttpStatus.CONFLICT.value(),
                    errorType = ErrorType.FAILED_SHIPPING_PLACEMENT,
                    message = e.message
                )
            )
    }

    private fun location(shippingDto: ShippingDto): URI = ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/api/v1/users/${shippingDto.userId}/shippings/${shippingDto.id}")
        .build().toUri()
}
