package com.codeperfection.shipit.service

import com.codeperfection.shipit.dto.common.PageDto
import com.codeperfection.shipit.dto.common.PaginationFilterDto
import com.codeperfection.shipit.dto.transporter.TransporterDto
import com.codeperfection.shipit.entity.Transporter
import com.codeperfection.shipit.exception.clienterror.NotFoundException
import com.codeperfection.shipit.fixture.*
import com.codeperfection.shipit.repository.TransporterRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.Clock
import java.time.ZoneId

@ExtendWith(MockitoExtension::class)
class TransporterServiceTest {

    @Mock
    private lateinit var transporterRepository: TransporterRepository

    @Mock
    private lateinit var transporterProvider: TransporterProvider

    @Mock
    private lateinit var authorizationService: AuthorizationService

    @Mock
    private lateinit var clock: Clock

    @InjectMocks
    private lateinit var underTest: TransporterService

    private val transporterFixture = createTransporterFixture()

    @AfterEach
    fun tearDown() {
        verifyNoMoreInteractions(transporterRepository, transporterProvider, authorizationService, clock)
    }

    @Test
    fun `GIVEN create transporter request, WHEN creating transporter, THEN it is saved in db and returned`() {
        whenever(clock.instant()).thenReturn(transporterFixture.createdAt.toInstant())
        whenever(clock.zone).thenReturn(ZoneId.of("UTC"))
        whenever(transporterRepository.save(any<Transporter>())).thenReturn(transporterFixture)

        val transporterDto = underTest.createTransporter(USER_ID, createTransporterDtoFixture)

        verify(clock).instant()
        verify(clock).zone
        verify(authorizationService).checkWriteAccess(USER_ID)
        val transporterArgumentCaptor = argumentCaptor<Transporter>()
        verify(transporterRepository).save(transporterArgumentCaptor.capture())
        val savedTransporter = transporterArgumentCaptor.firstValue
        assertThat(savedTransporter).usingRecursiveComparison().ignoringFields("id").isEqualTo(transporterFixture)
        assertThat(transporterDto).isEqualTo(transporterDtoFixture)
    }

    @Test
    fun `GIVEN pagination filter, WHEN getting empty transporters, THEN an empty transporters page is returned`() {
        val pageRequest = PageRequest.of(0, 100, Sort.by("createdAt"))
        whenever(
            transporterRepository.findByUserIdAndIsActiveTrue(
                USER_ID,
                pageRequest
            )
        ).thenReturn(PageImpl(emptyList()))

        val transportersPage = underTest.getTransporters(USER_ID, PaginationFilterDto())

        assertThat(transportersPage).isEqualTo(
            PageDto<TransporterDto>(
                totalElements = 0,
                totalPages = 1,
                elements = emptyList()
            )
        )
        verify(authorizationService).checkReadAccess(USER_ID)
        verify(transporterRepository).findByUserIdAndIsActiveTrue(USER_ID, pageRequest)
    }

    @Test
    fun `GIVEN pagination filter, WHEN getting transporters, THEN transporters page is returned`() {
        val pageRequest = PageRequest.of(0, 100, Sort.by("createdAt"))
        whenever(transporterRepository.findByUserIdAndIsActiveTrue(USER_ID, pageRequest))
            .thenReturn(PageImpl(listOf(transporterFixture)))

        val transportersPage = underTest.getTransporters(USER_ID, PaginationFilterDto())

        assertThat(transportersPage).isEqualTo(
            PageDto(totalElements = 1, totalPages = 1, elements = listOf(transporterDtoFixture))
        )
        verify(transporterRepository).findByUserIdAndIsActiveTrue(USER_ID, pageRequest)
        verify(authorizationService).checkReadAccess(USER_ID)
    }

    @Test
    fun `WHEN getting transporter, THEN expected dto is returned`() {
        whenever(transporterProvider.getTransporter(USER_ID, TRANSPORTER_ID)).thenReturn(transporterFixture)

        assertThat(underTest.getTransporter(USER_ID, TRANSPORTER_ID)).isEqualTo(transporterDtoFixture)

        verify(authorizationService).checkReadAccess(USER_ID)
        verify(transporterProvider).getTransporter(USER_ID, TRANSPORTER_ID)
    }

    @Test
    fun `GIVEN transporter with id and user id doesn't exist, WHEN deleting transporter, THEN exception is thrown`() {
        whenever(transporterRepository.findByIdAndUserIdAndIsActiveTrue(TRANSPORTER_ID, USER_ID)).thenReturn(null)

        assertThrows<NotFoundException> {
            underTest.deleteTransporter(USER_ID, TRANSPORTER_ID)

        }
        verify(authorizationService).checkWriteAccess(USER_ID)
        verify(transporterRepository).findByIdAndUserIdAndIsActiveTrue(TRANSPORTER_ID, USER_ID)
    }

    @Test
    fun `GIVEN transporter with id and user id exists, WHEN deleting transporter, THEN it is updated in db`() {
        whenever(transporterRepository.findByIdAndUserIdAndIsActiveTrue(TRANSPORTER_ID, USER_ID)).thenReturn(
            transporterFixture
        )
        val deactivatedTransporter = transporterFixture.copy(isActive = false)
        whenever(transporterRepository.save(deactivatedTransporter)).thenReturn(deactivatedTransporter)

        underTest.deleteTransporter(USER_ID, TRANSPORTER_ID)

        verify(authorizationService).checkWriteAccess(USER_ID)
        verify(transporterRepository).findByIdAndUserIdAndIsActiveTrue(TRANSPORTER_ID, USER_ID)
        verify(transporterRepository).save(deactivatedTransporter)
    }
}
