package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.dto.PageDto;
import com.codeperfection.shipit.dto.PaginationFilterDto;
import com.codeperfection.shipit.dto.TransporterDto;
import com.codeperfection.shipit.security.AuthenticatedUser;
import com.codeperfection.shipit.service.TransporterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(RequestValues.API_V1 + RequestValues.TRANSPORTERS)
public class TransporterController {

    private TransporterService transporterService;

    public TransporterController(TransporterService transporterService) {
        this.transporterService = transporterService;
    }

    @PostMapping
    public ResponseEntity<TransporterDto> createTransporter(
            @Valid @RequestBody TransporterDto transporterDto,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        final var transporter = transporterService.save(transporterDto, authenticatedUser);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path(RequestValues.UUID_PARAM)
                .buildAndExpand(transporter.getUuid()).toUri();
        return ResponseEntity.created(location).body(transporter);
    }

    @GetMapping
    public ResponseEntity<PageDto<TransporterDto>> getTransporters(
            @Valid PaginationFilterDto paginationFilterDto,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(transporterService.getTransporters(paginationFilterDto, authenticatedUser));
    }

    @GetMapping(RequestValues.UUID_PARAM)
    public ResponseEntity<TransporterDto> getTransporter(
            @PathVariable UUID uuid, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(transporterService.getTransporter(uuid, authenticatedUser));
    }
}
