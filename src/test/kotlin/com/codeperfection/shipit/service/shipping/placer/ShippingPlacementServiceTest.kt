package com.codeperfection.shipit.service.shipping.placer

import com.codeperfection.shipit.fixture.*
import com.codeperfection.shipit.repository.ProductRepository
import com.codeperfection.shipit.repository.ShippingRepository
import com.codeperfection.shipit.service.AuthorizationService
import com.codeperfection.shipit.service.TransporterProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class ShippingPlacementServiceTest {

    @Mock
    private lateinit var shippingRepository: ShippingRepository

    @Mock
    private lateinit var transporterProvider: TransporterProvider

    @Mock
    private lateinit var productRepository: ProductRepository

    @Mock
    private lateinit var shippingFactory: ShippingFactory

    @Mock
    private lateinit var authorizationService: AuthorizationService

    @InjectMocks
    private lateinit var underTest: ShippingPlacementService

    @AfterEach
    fun tearDown() {
        verifyNoMoreInteractions(shippingRepository, transporterProvider, productRepository, shippingFactory)
    }

    @Test
    fun `WHEN creating shipping, THEN expected dependencies are called and dto returned`() {
        val productFixture1 = createProductFixture1()
        val productFixture2 = createProductFixture2()
        val productFixtures = listOf(productFixture1, productFixture2)
        whenever(productRepository.findAllByUserIdAndIsActiveTrue(USER_ID)).thenReturn(productFixtures)
        val transporterFixture = createTransporterFixture()
        whenever(transporterProvider.getTransporter(USER_ID, TRANSPORTER_ID)).thenReturn(transporterFixture)
        val shippingFixture = createShippingFixture()
        whenever(shippingFactory.create(SHIPPING_NAME, productFixtures, transporterFixture)).thenReturn(shippingFixture)

        underTest.createShipping(USER_ID, createShippingDtoFixture)

        verify(authorizationService).checkWriteAccess(USER_ID)
        verify(productRepository).findAllByUserIdAndIsActiveTrue(USER_ID)
        verify(transporterProvider).getTransporter(USER_ID, TRANSPORTER_ID)
        verify(shippingFactory).create(SHIPPING_NAME, productFixtures, transporterFixture)
        verify(shippingRepository).save(shippingFixture)
        verify(productRepository).save(productFixture1.copy(countInStock = 5))
        verify(productRepository).save(productFixture2.copy(countInStock = 5))
    }
}
