package com.codeperfection.shipit.service

import com.codeperfection.shipit.exception.clienterror.NotFoundException
import com.codeperfection.shipit.fixture.TRANSPORTER_ID
import com.codeperfection.shipit.fixture.USER_ID
import com.codeperfection.shipit.fixture.createTransporterFixture
import com.codeperfection.shipit.repository.TransporterRepository
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

@ExtendWith(MockitoExtension::class)
class TransporterProviderTest {

    @Mock
    private lateinit var transporterRepository: TransporterRepository

    @InjectMocks
    private lateinit var underTest: TransporterProvider

    @AfterEach
    fun tearDown() {
        verifyNoMoreInteractions(transporterRepository)
    }

    @Test
    fun `GIVEN transporter with id and user id doesn't exist, WHEN getting transporter, THEN exception is thrown`() {
        whenever(transporterRepository.findByIdAndUserIdAndIsActiveTrue(TRANSPORTER_ID, USER_ID)).thenReturn(null)

        assertThrows<NotFoundException> { underTest.getTransporter(USER_ID, TRANSPORTER_ID) }

        verify(transporterRepository).findByIdAndUserIdAndIsActiveTrue(TRANSPORTER_ID, USER_ID)
    }

    @Test
    fun `GIVEN transporter with id and user id exists, WHEN getting transporter, THEN it is returned`() {
        val transporterFixture = createTransporterFixture()
        whenever(transporterRepository.findByIdAndUserIdAndIsActiveTrue(TRANSPORTER_ID, USER_ID))
            .thenReturn(transporterFixture)

        assertThat(underTest.getTransporter(USER_ID, TRANSPORTER_ID)).isEqualTo(transporterFixture)

        verify(transporterRepository).findByIdAndUserIdAndIsActiveTrue(TRANSPORTER_ID, USER_ID)
    }
}
