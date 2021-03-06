package com.codeperfection.shipit.controller;

import com.codeperfection.shipit.dto.common.PageDto;
import com.codeperfection.shipit.dto.common.PaginationFilterDto;
import com.codeperfection.shipit.dto.product.CreateProductDto;
import com.codeperfection.shipit.dto.product.ProductDto;
import com.codeperfection.shipit.dto.product.UpdateCountInStockDto;
import com.codeperfection.shipit.dto.product.UpdateProductDto;
import com.codeperfection.shipit.security.AuthenticatedUser;
import com.codeperfection.shipit.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

import static com.codeperfection.shipit.controller.CommonPathValues.API_V1;

@RestController
@RequestMapping(API_V1 + ProductController.PRODUCTS_PATH)
public class ProductController {

    static final String PRODUCTS_PATH = "/products";

    static final String PRODUCT_UUID_PATH = "/{productUuid}";

    static final String COUNT_IN_STOCK_PATH = "/count-in-stock";

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody CreateProductDto createProductDto,
                                                    @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        final var product = productService.createProduct(createProductDto, authenticatedUser);
        return ResponseEntity.created(getLocation(product.getUuid())).body(product);
    }

    @GetMapping
    public ResponseEntity<PageDto<ProductDto>> getProducts(
            @Valid PaginationFilterDto paginationFilterDto,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(productService.getProducts(paginationFilterDto, authenticatedUser));
    }

    @GetMapping(PRODUCT_UUID_PATH)
    public ResponseEntity<ProductDto> getProduct(@PathVariable UUID productUuid,
                                                 @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(productService.getProduct(productUuid, authenticatedUser));
    }

    @PutMapping(PRODUCT_UUID_PATH)
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable UUID productUuid, @Valid @RequestBody UpdateProductDto updateProductDto,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        final var product = productService.updateProduct(productUuid, updateProductDto, authenticatedUser);
        // Please note that location of the product changes, as new version with latest state is created
        return ResponseEntity.status(HttpStatus.OK).location(getLocation(product.getUuid())).body(product);
    }

    @PutMapping(PRODUCT_UUID_PATH + COUNT_IN_STOCK_PATH)
    public ResponseEntity<ProductDto> updateCountInStock(
            @PathVariable UUID productUuid, @Valid @RequestBody UpdateCountInStockDto updateCountInStockDto,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(productService.updateCountInStock(
                productUuid, updateCountInStockDto, authenticatedUser));
    }

    @DeleteMapping(PRODUCT_UUID_PATH)
    public void deleteProduct(@PathVariable UUID productUuid,
                              @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        productService.deleteProduct(productUuid, authenticatedUser);
    }

    private URI getLocation(UUID productUuid) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(API_V1 + PRODUCTS_PATH + PRODUCT_UUID_PATH)
                .buildAndExpand(productUuid).toUri();
    }
}
