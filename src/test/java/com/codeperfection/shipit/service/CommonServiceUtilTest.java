package com.codeperfection.shipit.service;

import com.codeperfection.shipit.entity.User;
import com.codeperfection.shipit.exception.clienterror.EntityNotFoundException;
import com.codeperfection.shipit.repository.TransporterRepository;
import com.codeperfection.shipit.util.TransporterFixtureFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommonServiceUtilTest {

    @Mock
    private TransporterRepository transporterRepository;

    @InjectMocks
    private CommonServiceUtil commonServiceUtil;

    @SuppressWarnings("unchecked")
    @Test
    public void applyChangeIfNeededOnNullUpdateValueDoesNotApplyAndReturnsFalse() {
        final Integer existingValue = 5;
        final Integer updateValue = null;
        final var setter = (Consumer<Integer>) mock(Consumer.class);

        assertThat(commonServiceUtil.applyChangeIfNeeded(existingValue, updateValue, setter)).isFalse();

        verifyNoMoreInteractions(setter, transporterRepository);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void applyChangeIfNeededOnEqualValuesDoesNothingAndReturnsFalse() {
        final Integer existingValue = 5;
        final Integer updateValue = 5;
        final var setter = (Consumer<Integer>) mock(Consumer.class);

        assertThat(commonServiceUtil.applyChangeIfNeeded(existingValue, updateValue, setter)).isFalse();

        verifyNoMoreInteractions(setter, transporterRepository);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void applyChangeIfNeededOnNotEqualValuesApplyTheChangeAndReturnTrue() {
        final Integer existingValue = 5;
        final Integer updateValue = 4;
        final var setter = (Consumer<Integer>) mock(Consumer.class);

        assertThat(commonServiceUtil.applyChangeIfNeeded(existingValue, updateValue, setter)).isTrue();

        verify(setter).accept(updateValue);
        verifyNoMoreInteractions(setter, transporterRepository);
    }

    @Test
    public void getTransporterIfNoTransporterFoundThrowsException() {
        final UUID transporterUuid = UUID.randomUUID();
        final var user = mock(User.class);
        doReturn(Optional.empty()).when(transporterRepository).findByUuidAndUser(transporterUuid, user);

        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                commonServiceUtil.getTransporter(transporterUuid, user));

        verify(transporterRepository).findByUuidAndUser(transporterUuid, user);
        verifyNoMoreInteractions(transporterRepository);
    }

    @Test
    public void getTransporterIfTransporterFoundReturnsEntity() {
        final UUID transporterUuid = UUID.randomUUID();
        final var user = mock(User.class);
        final var transporter = TransporterFixtureFactory.createTransporter();

        doReturn(Optional.of(transporter)).when(transporterRepository).findByUuidAndUser(transporterUuid, user);

        assertThat(commonServiceUtil.getTransporter(transporterUuid, user)).isEqualTo(transporter);

        verify(transporterRepository).findByUuidAndUser(transporterUuid, user);
        verifyNoMoreInteractions(transporterRepository);
    }
}
