package com.codeperfection.shipit.service

import com.codeperfection.shipit.dto.common.PageDto
import com.codeperfection.shipit.dto.common.PaginationFilterDto
import com.codeperfection.shipit.dto.product.CreateProductDto
import com.codeperfection.shipit.dto.product.ProductDto
import com.codeperfection.shipit.dto.product.UpdateProductDto
import com.codeperfection.shipit.entity.Product
import com.codeperfection.shipit.exception.clienterror.NotFoundException
import com.codeperfection.shipit.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val clock: Clock
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    @PreAuthorize("hasAuthority('SCOPE_shipit:write')")
    fun createProduct(userId: UUID, createProductDto: CreateProductDto): ProductDto {
        val now = OffsetDateTime.now(clock)
        val id = UUID.randomUUID()
        val savedProduct = productRepository.save(
            Product(
                id = UUID.randomUUID(),
                userId = userId,
                name = createProductDto.name,
                volume = createProductDto.volume,
                price = createProductDto.price,
                countInStock = createProductDto.countInStock,
                createdAt = now,
                updatedAt = now,
                isActive = true
            )
        )

        logger.info("Created product '${savedProduct.name}' with ID $id for user with ID $userId")
        return ProductDto.fromEntity(savedProduct)
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCOPE_shipit:read')")
    fun getProducts(userId: UUID, paginationFilterDto: PaginationFilterDto): PageDto<ProductDto> {
        val productsPage = productRepository.findByUserIdAndIsActiveTrue(userId, paginationFilterDto.toPageable())

        return PageDto(
            totalElements = productsPage.totalElements,
            totalPages = productsPage.totalPages,
            elements = productsPage.toList().map(ProductDto::fromEntity)
        )
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCOPE_shipit:read')")
    fun getProduct(userId: UUID, productId: UUID): ProductDto {
        val product = productRepository.findByIdAndUserIdAndIsActiveTrue(productId, userId)
            ?: throw NotFoundException(productId, userId)
        return ProductDto.fromEntity(product)
    }

    @Transactional
    @PreAuthorize("hasAuthority('SCOPE_shipit:write')")
    fun updateProduct(userId: UUID, productId: UUID, updateProductDto: UpdateProductDto): ProductDto {
        val product = productRepository.findByIdAndUserIdAndIsActiveTrue(productId, userId)
            ?: throw NotFoundException(productId, userId)
        product.countInStock = updateProductDto.countInStock
        val savedProduct = productRepository.save(product)

        logger.info("Updated product '${savedProduct.name}' with ID ${savedProduct.id} for user with ID $userId")
        return ProductDto.fromEntity(savedProduct)
    }

    @Transactional
    @PreAuthorize("hasAuthority('SCOPE_shipit:write')")
    fun deleteProduct(userId: UUID, productId: UUID) {
        val product = productRepository.findByIdAndUserIdAndIsActiveTrue(productId, userId)
            ?: throw NotFoundException(productId, userId)
        product.isActive = false
        productRepository.save(product)
        logger.info("Deleted product '${product.name}' with ID ${product.id} for user with ID $userId")
    }
}
