package com.codeperfection.shipit.controller

import com.codeperfection.shipit.dto.PageDto
import com.codeperfection.shipit.dto.PaginationFilterDto
import com.codeperfection.shipit.dto.transporter.CreateTransporterDto
import com.codeperfection.shipit.dto.transporter.TransporterDto
import com.codeperfection.shipit.service.TransporterService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/users/{userId}/transporters")
class TransporterController(private val transporterService: TransporterService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTransporter(
        @PathVariable userId: UUID,
        @Valid @RequestBody createTransporterDto: CreateTransporterDto
    ): TransporterDto =
        transporterService.createTransporter(userId, createTransporterDto)

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
}
