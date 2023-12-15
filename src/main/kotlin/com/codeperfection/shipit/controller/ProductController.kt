package com.codeperfection.shipit.controller

import com.codeperfection.shipit.dto.PageDto
import com.codeperfection.shipit.dto.PaginationFilterDto
import com.codeperfection.shipit.dto.product.CreateProductDto
import com.codeperfection.shipit.dto.product.ProductDto
import com.codeperfection.shipit.dto.product.UpdateProductDto
import com.codeperfection.shipit.service.ProductService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/users/{userId}/products")
class ProductController(private val productService: ProductService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(@PathVariable userId: UUID, @Valid @RequestBody createProductDto: CreateProductDto): ProductDto =
        productService.createProduct(userId, createProductDto)

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
}
