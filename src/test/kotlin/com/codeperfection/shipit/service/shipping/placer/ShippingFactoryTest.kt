package com.codeperfection.shipit.service.shipping.placer

import com.codeperfection.shipit.exception.ShippingImpossibleException
import com.codeperfection.shipit.fixture.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.Clock
import java.time.ZoneId

@ExtendWith(MockitoExtension::class)
class ShippingFactoryTest {

    @Mock
    private lateinit var knapsackPlacer: KnapsackPlacer

    @Mock
    private lateinit var clock: Clock

    @InjectMocks
    private lateinit var underTest: ShippingFactory

    @AfterEach
    fun tearDown() {
        verifyNoMoreInteractions(knapsackPlacer, clock)
    }

    @Test
    fun `GIVEN empty product list, WHEN creating a shipping, THEN exception is thrown`() {
        assertThrows<ShippingImpossibleException> {
            underTest.create(SHIPPING_NAME, emptyList(), createTransporterFixture())
        }
    }

    @Test
    fun `GIVEN placer returns empty Knapsack, WHEN creating a shipping, THEN exception is thrown`() {
        val productFixtures = listOf(createProductFixture1(), createProductFixture2())
        whenever(knapsackPlacer.place(anyArray<Knapsack.Item>(), eq(TRANSPORTER_CAPACITY))).thenReturn(emptyKnapsack)

        assertThrows<ShippingImpossibleException> {
            underTest.create(SHIPPING_NAME, productFixtures, createTransporterFixture())
        }

        val itemsArgumentCaptor = argumentCaptor<Array<Knapsack.Item>>()
        verify(knapsackPlacer).place(itemsArgumentCaptor.capture(), eq(TRANSPORTER_CAPACITY))
        assertThat(itemsArgumentCaptor.firstValue).containsExactlyInAnyOrderElementsOf(
            List(size = 7) { product1ItemFixture } + List(9) { product2ItemFixture }
        )
    }

    @Test
    fun `GIVEN placer returns non-empty Knapsack, WHEN creating a shipping, THEN expected shipping is returned`() {
        val productFixtures = listOf(createProductFixture1(), createProductFixture2())
        whenever(knapsackPlacer.place(anyArray<Knapsack.Item>(), eq(TRANSPORTER_CAPACITY))).thenReturn(
            Knapsack(
                capacity = TRANSPORTER_CAPACITY,
                totalPrice = 88,
                items = List(size = 2) { product1ItemFixture } + List(size = 4) { product2ItemFixture }
            )
        )
        whenever(clock.zone).thenReturn(ZoneId.of("UTC"))
        whenever(clock.instant()).thenReturn(SHIPPING_CREATED_AT.toInstant())

        val result = underTest.create(SHIPPING_NAME, productFixtures, createTransporterFixture())
        val expected = createShippingFixture()
        assertThat(result).usingRecursiveComparison().ignoringFields("id", "shippedItems")
            .isEqualTo(expected)
        assertThat(result.shippedItems).usingRecursiveComparison().ignoringFields("id", "shipping")
            .isEqualTo(expected.shippedItems)

        val itemsArgumentCaptor = argumentCaptor<Array<Knapsack.Item>>()
        verify(knapsackPlacer).place(itemsArgumentCaptor.capture(), eq(TRANSPORTER_CAPACITY))
        assertThat(itemsArgumentCaptor.firstValue).containsExactlyInAnyOrderElementsOf(
            List(size = 7) { product1ItemFixture } + List(size = 9) { product2ItemFixture }
        )
        verify(clock).zone
        verify(clock).instant()
    }
}
