package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.dto.common.PageDto;
import com.codeperfection.shipit.dto.common.PaginationFilterDto;
import com.codeperfection.shipit.dto.transporter.CreateTransporterDto;
import com.codeperfection.shipit.dto.transporter.TransporterDto;
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
@RequestMapping(CommonPathValues.API_V1 + TransporterController.TRANSPORTERS_PATH)
public class TransporterController {

    static final String TRANSPORTERS_PATH = "/transporters";

    static final String TRANSPORTER_UUID_PATH = "/{transporterUuid}";

    private TransporterService transporterService;

    public TransporterController(TransporterService transporterService) {
        this.transporterService = transporterService;
    }

    @PostMapping
    public ResponseEntity<TransporterDto> createTransporter(
            @Valid @RequestBody CreateTransporterDto createTransporterDto,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        final var transporter = transporterService.save(createTransporterDto, authenticatedUser);
        return ResponseEntity.created(getLocation(transporter.getUuid())).body(transporter);
    }

    @GetMapping
    public ResponseEntity<PageDto<TransporterDto>> getTransporters(
            @Valid PaginationFilterDto paginationFilterDto,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(transporterService.getTransporters(paginationFilterDto, authenticatedUser));
    }

    @GetMapping(TRANSPORTER_UUID_PATH)
    public ResponseEntity<TransporterDto> getTransporter(
            @PathVariable UUID transporterUuid, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(transporterService.getTransporter(transporterUuid, authenticatedUser));
    }

    private URI getLocation(UUID transporterUuid) {
        return ServletUriComponentsBuilder.fromCurrentRequest().path(TRANSPORTER_UUID_PATH)
                .buildAndExpand(transporterUuid).toUri();
    }
}
