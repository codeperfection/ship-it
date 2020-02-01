package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.dto.CreateShippingDto;
import com.codeperfection.shipit.dto.ShippingDto;
import com.codeperfection.shipit.security.AuthenticatedUser;
import com.codeperfection.shipit.service.ShippingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

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
}
