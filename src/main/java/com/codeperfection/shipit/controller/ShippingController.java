package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.dto.CreateShippingDto;
import com.codeperfection.shipit.dto.PageDto;
import com.codeperfection.shipit.dto.PaginationFilterDto;
import com.codeperfection.shipit.dto.ShippingDto;
import com.codeperfection.shipit.security.AuthenticatedUser;
import com.codeperfection.shipit.service.ShippingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(RequestValues.API_V1 + RequestValues.SHIPPINGS)
public class ShippingController {

    private ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @PostMapping
    public ResponseEntity<ShippingDto> createShipping(@Valid @RequestBody CreateShippingDto createShippingDto,
                                                      @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        final var shipping = shippingService.createShipping(createShippingDto, authenticatedUser);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path(RequestValues.UUID_PARAM)
                .buildAndExpand(shipping.getUuid()).toUri();
        return ResponseEntity.created(location).body(shipping);
    }

    @GetMapping
    public ResponseEntity<PageDto<ShippingDto>> getShippings(
            @Valid PaginationFilterDto paginationFilterDto,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(shippingService.getShippings(paginationFilterDto, authenticatedUser));
    }

    @GetMapping(RequestValues.UUID_PARAM)
    public ResponseEntity<ShippingDto> getShipping(
            @PathVariable UUID uuid, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(shippingService.getShipping(uuid, authenticatedUser));
    }
}
