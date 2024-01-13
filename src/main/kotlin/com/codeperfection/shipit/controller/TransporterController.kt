package com.codeperfection.shipit.controller

import com.codeperfection.shipit.dto.common.PageDto
import com.codeperfection.shipit.dto.common.PaginationFilterDto
import com.codeperfection.shipit.dto.transporter.CreateTransporterDto
import com.codeperfection.shipit.dto.transporter.TransporterDto
import com.codeperfection.shipit.service.TransporterService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/api/v1/users/{userId}/transporters")
class TransporterController(private val transporterService: TransporterService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTransporter(
        @PathVariable userId: UUID,
        @Valid @RequestBody createTransporterDto: CreateTransporterDto
    ): ResponseEntity<TransporterDto> {
        val transporterDto = transporterService.createTransporter(userId, createTransporterDto)
        return ResponseEntity.created(location(transporterDto)).body(transporterDto)
    }

    @GetMapping
    fun getTransporters(
        @PathVariable userId: UUID,
        @Valid paginationFilterDto: PaginationFilterDto
    ): PageDto<TransporterDto> =
        transporterService.getTransporters(userId, paginationFilterDto)

    @GetMapping("/{transporterId}")
    fun getTransporter(@PathVariable userId: UUID, @PathVariable transporterId: UUID): TransporterDto =
        transporterService.getTransporter(userId, transporterId)

    @DeleteMapping("/{transporterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTransporter(@PathVariable userId: UUID, @PathVariable transporterId: UUID) =
        transporterService.deleteTransporter(userId, transporterId)

    private fun location(transporterDto: TransporterDto): URI = ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/api/v1/users/${transporterDto.userId}/transporters/${transporterDto.id}")
        .build().toUri()
}
