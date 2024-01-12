package com.codeperfection.shipit.service

import com.codeperfection.shipit.dto.PageDto
import com.codeperfection.shipit.dto.PaginationFilterDto
import com.codeperfection.shipit.dto.product.CreateProductDto
import com.codeperfection.shipit.dto.product.ProductDto
import com.codeperfection.shipit.dto.product.UpdateProductDto
import com.codeperfection.shipit.entity.Product
import com.codeperfection.shipit.exception.clienterror.NotFoundException
import com.codeperfection.shipit.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val authenticationService: AuthenticationService,
    private val clock: Clock
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun createProduct(userId: UUID, createProductDto: CreateProductDto): ProductDto {
        authenticationService.checkWriteAccess(userId)
        logger.info("Creating product ${createProductDto.name} for user $userId")
        val now = OffsetDateTime.now(clock)
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

        return mapToDto(savedProduct)
    }

    @Transactional(readOnly = true)
    fun getProducts(userId: UUID, paginationFilterDto: PaginationFilterDto): PageDto<ProductDto> {
        authenticationService.checkReadAccess(userId)
        val productsPage = productRepository.findByUserIdAndIsActiveTrue(
            userId = userId,
            pageable = PageRequest.of(paginationFilterDto.page, paginationFilterDto.size, Sort.by("createdAt"))
        )

        return PageDto(
            totalElements = productsPage.totalElements,
            totalPages = productsPage.totalPages,
            elements = productsPage.toList().map { mapToDto(it) }
        )
    }

    @Transactional(readOnly = true)
    fun getProduct(userId: UUID, productId: UUID): ProductDto {
        authenticationService.checkReadAccess(userId)
        val product = productRepository.findByIdAndUserIdAndIsActiveTrue(productId, userId)
            ?: throw NotFoundException(productId)
        return mapToDto(product)
    }

    @Transactional
    fun updateProduct(userId: UUID, productId: UUID, updateProductDto: UpdateProductDto): ProductDto {
        authenticationService.checkWriteAccess(userId)
        val product = productRepository.findByIdAndUserIdAndIsActiveTrue(productId, userId)
            ?: throw NotFoundException(productId)
        product.countInStock = updateProductDto.countInStock
        val savedProduct = productRepository.save(product)
        return mapToDto(savedProduct)
    }

    @Transactional
    fun deleteProduct(userId: UUID, productId: UUID) {
        authenticationService.checkWriteAccess(userId)
        val product = productRepository.findByIdAndUserIdAndIsActiveTrue(productId, userId)
            ?: throw NotFoundException(productId)
        product.isActive = false
        productRepository.save(product)
    }

    private fun mapToDto(product: Product) = ProductDto(
        id = product.id,
        userId = product.userId,
        name = product.name,
        volume = product.volume,
        price = product.price,
        countInStock = product.countInStock
    )
}
