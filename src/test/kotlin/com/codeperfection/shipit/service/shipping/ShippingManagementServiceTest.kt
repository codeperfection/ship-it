package com.codeperfection.shipit.service.shipping

import com.codeperfection.shipit.dto.common.PageDto
import com.codeperfection.shipit.dto.common.PaginationFilterDto
import com.codeperfection.shipit.dto.shipping.ShippingDto
import com.codeperfection.shipit.exception.clienterror.NotFoundException
import com.codeperfection.shipit.fixture.SHIPPING_ID
import com.codeperfection.shipit.fixture.USER_ID
import com.codeperfection.shipit.fixture.createShippingFixture
import com.codeperfection.shipit.fixture.shippingDtoFixture
import com.codeperfection.shipit.repository.ShippingRepository
import com.codeperfection.shipit.service.AuthorizationService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@ExtendWith(MockitoExtension::class)
class ShippingManagementServiceTest {

    @Mock
    private lateinit var shippingRepository: ShippingRepository

    @Mock
    private lateinit var authorizationService: AuthorizationService

    @InjectMocks
    private lateinit var underTest: ShippingManagementService

    @AfterEach
    fun tearDown() {
        verifyNoMoreInteractions(shippingRepository, authorizationService)
    }

    private val shippingFixture = createShippingFixture()

    @Test
    fun `GIVEN pagination filter, WHEN getting empty shippings, THEN an empty shippings page is returned`() {
        val pageRequest = PageRequest.of(0, 100, Sort.by("createdAt"))
        whenever(shippingRepository.findByUserId(USER_ID, pageRequest)).thenReturn(PageImpl(emptyList()))

        val shippingsPage = underTest.getShippings(USER_ID, PaginationFilterDto())

        assertThat(shippingsPage).isEqualTo(
            PageDto<ShippingDto>(
                totalElements = 0,
                totalPages = 1,
                elements = emptyList()
            )
        )

        verify(authorizationService).checkReadAccess(USER_ID)
        verify(shippingRepository).findByUserId(USER_ID, pageRequest)
    }

    @Test
    fun `GIVEN pagination filter, WHEN getting shippings, THEN shippings page is returned`() {
        val pageRequest = PageRequest.of(0, 100, Sort.by("createdAt"))
        whenever(shippingRepository.findByUserId(USER_ID, pageRequest)).thenReturn(PageImpl(listOf(shippingFixture)))

        val shippingsPage = underTest.getShippings(USER_ID, PaginationFilterDto())
        assertThat(shippingsPage).isEqualTo(
            PageDto(totalElements = 1, totalPages = 1, elements = listOf(shippingDtoFixture))
        )

        verify(authorizationService).checkReadAccess(USER_ID)
        verify(shippingRepository).findByUserId(USER_ID, pageRequest)
    }

    @Test
    fun `GIVEN shipping with id and user id doesn't exist, WHEN getting shipping, THEN exception is thrown`() {
        whenever(shippingRepository.findByIdAndUserId(SHIPPING_ID, USER_ID)).thenReturn(null)

        assertThrows<NotFoundException> {
            underTest.getShipping(USER_ID, SHIPPING_ID)
        }

        verify(authorizationService).checkReadAccess(USER_ID)
        verify(shippingRepository).findByIdAndUserId(SHIPPING_ID, USER_ID)
    }

    @Test
    fun `GIVEN shipping with id and user id exists, WHEN getting shipping, THEN it is returned`() {
        whenever(shippingRepository.findByIdAndUserId(SHIPPING_ID, USER_ID)).thenReturn(shippingFixture)

        val shippingDto = underTest.getShipping(USER_ID, SHIPPING_ID)
        assertThat(shippingDto).isEqualTo(shippingDtoFixture)

        verify(authorizationService).checkReadAccess(USER_ID)
        verify(shippingRepository).findByIdAndUserId(SHIPPING_ID, USER_ID)
    }

    @Test
    fun `GIVEN shipping with id and user id doesn't exist, WHEN deleting shipping, THEN exception is thrown`() {
        whenever(shippingRepository.findByIdAndUserId(SHIPPING_ID, USER_ID)).thenReturn(null)

        assertThrows<NotFoundException> {
            underTest.deleteShipping(USER_ID, SHIPPING_ID)
        }

        verify(authorizationService).checkWriteAccess(USER_ID)
        verify(shippingRepository).findByIdAndUserId(SHIPPING_ID, USER_ID)
    }

    @Test
    fun `GIVEN shipping with id and user id exists, WHEN deleting shipping, THEN it is deleted`() {
        whenever(shippingRepository.findByIdAndUserId(SHIPPING_ID, USER_ID)).thenReturn(shippingFixture)

        underTest.deleteShipping(USER_ID, SHIPPING_ID)

        verify(authorizationService).checkWriteAccess(USER_ID)
        verify(shippingRepository).findByIdAndUserId(SHIPPING_ID, USER_ID)
        verify(shippingRepository).delete(shippingFixture)
    }
}
