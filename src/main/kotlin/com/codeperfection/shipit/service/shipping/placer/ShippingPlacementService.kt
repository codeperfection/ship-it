package com.codeperfection.shipit.service.shipping.placer

import com.codeperfection.shipit.dto.shipping.CreateShippingDto
import com.codeperfection.shipit.dto.shipping.ShippingDto
import com.codeperfection.shipit.entity.Product
import com.codeperfection.shipit.repository.ProductRepository
import com.codeperfection.shipit.repository.ShippingRepository
import com.codeperfection.shipit.service.AuthorizationService
import com.codeperfection.shipit.service.TransporterProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ShippingPlacementService(
    private val shippingRepository: ShippingRepository,
    private val transporterProvider: TransporterProvider,
    private val productRepository: ProductRepository,
    private val shippingFactory: ShippingFactory,
    private val authorizationService: AuthorizationService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun createShipping(userId: UUID, createShippingDto: CreateShippingDto): ShippingDto {
        authorizationService.checkWriteAccess(userId)
        val shipping = shippingFactory.create(
            name = createShippingDto.name,
            products = productRepository.findAllByUserIdAndIsActiveTrue(userId),
            transporter = transporterProvider.getTransporter(userId, createShippingDto.transporterId)
        )

        shippingRepository.save(shipping)
        deductProductStock(shipping.shippedItems.associate { it.product to it.quantity })

        logger.info("Created shipping '${createShippingDto.name}' with ID ${shipping.id} for user with ID $userId")
        return ShippingDto.fromEntity(shipping)
    }

    private fun deductProductStock(productsToShip: Map<Product, Int>) {
        productsToShip.forEach { (product, quantity) ->
            productRepository.save(product.copy(countInStock = (product.countInStock - quantity)))
        }
    }
}
