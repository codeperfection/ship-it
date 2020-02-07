package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.dto.common.PageDto;
import com.codeperfection.shipit.dto.common.PaginationFilterDto;
import com.codeperfection.shipit.dto.shipping.CreateShippingDto;
import com.codeperfection.shipit.dto.shipping.ShippingDto;
import com.codeperfection.shipit.security.AuthenticatedUser;
import com.codeperfection.shipit.service.shipping.ShippingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(CommonPathValues.API_V1 + ShippingController.SHIPPINGS_PATH)
public class ShippingController {

    static final String SHIPPINGS_PATH = "/shippings";

    static final String SHIPPING_UUID_PATH = "/{shippingUuid}";

    private ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @PostMapping
    public ResponseEntity<ShippingDto> createShipping(@Valid @RequestBody CreateShippingDto createShippingDto,
                                                      @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        final var shipping = shippingService.createShipping(createShippingDto, authenticatedUser);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path(SHIPPING_UUID_PATH)
                .buildAndExpand(shipping.getUuid()).toUri();
        return ResponseEntity.created(location).body(shipping);
    }

    @GetMapping
    public ResponseEntity<PageDto<ShippingDto>> getShippings(
            @Valid PaginationFilterDto paginationFilterDto,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(shippingService.getShippings(paginationFilterDto, authenticatedUser));
    }

    @GetMapping(SHIPPING_UUID_PATH)
    public ResponseEntity<ShippingDto> getShipping(
            @PathVariable UUID shippingUuid, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(shippingService.getShipping(shippingUuid, authenticatedUser));
    }

    @DeleteMapping(SHIPPING_UUID_PATH)
    public void deleteShipping(@PathVariable UUID shippingUuid,
                               @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        shippingService.deleteShipping(shippingUuid, authenticatedUser);
    }
}
