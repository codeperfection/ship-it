package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.dto.PageDto;
import com.codeperfection.shipit.dto.PaginationFilterDto;
import com.codeperfection.shipit.dto.ProductDto;
import com.codeperfection.shipit.security.AuthenticatedUser;
import com.codeperfection.shipit.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(RequestValues.API_V1 + RequestValues.PRODUCTS)
public class ProductController {

    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto,
                                                    @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        final var product = productService.save(productDto, authenticatedUser);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path(RequestValues.UUID_PARAM)
                .buildAndExpand(product.getUuid()).toUri();
        return ResponseEntity.created(location).body(product);
    }

    @GetMapping
    public ResponseEntity<PageDto<ProductDto>> getProducts(
            @Valid PaginationFilterDto paginationFilterDto,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(productService.getProducts(paginationFilterDto, authenticatedUser));
    }

    @GetMapping(RequestValues.UUID_PARAM)
    public ResponseEntity<ProductDto> getProduct(@PathVariable UUID uuid,
                                                 @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(productService.getProduct(uuid, authenticatedUser));
    }
}
