package com.codeperfection.shipit.controller

import com.codeperfection.shipit.dto.common.PageDto
import com.codeperfection.shipit.dto.common.PaginationFilterDto
import com.codeperfection.shipit.dto.product.CreateProductDto
import com.codeperfection.shipit.dto.product.ProductDto
import com.codeperfection.shipit.dto.product.UpdateProductDto
import com.codeperfection.shipit.service.ProductService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/api/v1/users/{userId}/products")
class ProductController(private val productService: ProductService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(
        @PathVariable userId: UUID,
        @Valid @RequestBody createProductDto: CreateProductDto
    ): ResponseEntity<ProductDto> {
        val productDto = productService.createProduct(userId, createProductDto)
        return ResponseEntity.created(location(productDto)).body(productDto)
    }

    @GetMapping
    fun getProducts(@PathVariable userId: UUID, @Valid paginationFilterDto: PaginationFilterDto): PageDto<ProductDto> =
        productService.getProducts(userId, paginationFilterDto)


    @GetMapping("/{productId}")
    fun getProduct(@PathVariable userId: UUID, @PathVariable productId: UUID): ProductDto =
        productService.getProduct(userId, productId)

    @PutMapping("/{productId}")
    fun updateProduct(
        @PathVariable userId: UUID,
        @PathVariable productId: UUID,
        @Valid @RequestBody updateProductDto: UpdateProductDto
    ): ProductDto =
        productService.updateProduct(userId, productId, updateProductDto)

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProduct(@PathVariable userId: UUID, @PathVariable productId: UUID) =
        productService.deleteProduct(userId, productId)

    private fun location(productDto: ProductDto): URI = ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/api/v1/users/${productDto.userId}/products/${productDto.id}")
        .build().toUri()
}
