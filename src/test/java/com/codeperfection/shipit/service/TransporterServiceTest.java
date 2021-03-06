package com.codeperfection.shipit.service;

import com.codeperfection.shipit.dto.common.PageDto;
import com.codeperfection.shipit.dto.common.PaginationFilterDto;
import com.codeperfection.shipit.dto.transporter.UpdateTransporterDto;
import com.codeperfection.shipit.entity.Transporter;
import com.codeperfection.shipit.entity.User;
import com.codeperfection.shipit.exception.clienterror.CannotChangeInactiveEntityException;
import com.codeperfection.shipit.repository.TransporterRepository;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import com.codeperfection.shipit.util.TransporterFixtureFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransporterServiceTest {

    @Mock
    private TransporterRepository transporterRepository;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private TransporterService transporterService;

    @Test
    public void createTransporter_IfValidDto_SavesEntity() {
        final var createTransporterDto = TransporterFixtureFactory.createCreateTransporterDto();
        final var transporterDto = TransporterFixtureFactory.createTransporterDto();
        final var transporter = TransporterFixtureFactory.createTransporter();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(transporter).when(transporterRepository).save(any());

        final var savedTransporterDto = transporterService.createTransporter(createTransporterDto, authenticatedUser);

        final var transporterArgumentCaptor = ArgumentCaptor.forClass(Transporter.class);
        verify(transporterRepository).save(transporterArgumentCaptor.capture());
        final var savedTransporter = transporterArgumentCaptor.getValue();

        assertThat(savedTransporter).usingRecursiveComparison().ignoringFields("user", "uuid", "createdAt")
                .isEqualTo(transporter);
        assertThat(savedTransporter.getCreatedAt()).isCloseToUtcNow(within(10, ChronoUnit.SECONDS));
        assertThat(savedTransporter.getUser().getUuid()).isEqualTo(authenticatedUser.getUuid());

        assertThat(savedTransporterDto).isEqualTo(transporterDto);
        verifyNoMoreInteractions(transporterRepository);
    }

    @Test
    public void getTransporters_ReturnsPaginatedDtos() {
        final var paginationFilterDto = new PaginationFilterDto(2, 1);
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var databasePage = new PageImpl<>(List.of(TransporterFixtureFactory.createTransporter()));
        doReturn(databasePage).when(transporterRepository).findByUserAndIsActiveTrue(
                User.withUuid(authenticatedUser.getUuid()), PageRequest.of(paginationFilterDto.getPage(),
                        paginationFilterDto.getSize(), Sort.by("createdAt")));

        final var transportersPage = transporterService.getTransporters(paginationFilterDto, authenticatedUser);

        assertThat(transportersPage).isEqualTo(new PageDto<>(databasePage.getTotalElements(),
                databasePage.getTotalPages(), List.of(TransporterFixtureFactory.createTransporterDto())));
        verifyNoMoreInteractions(transporterRepository);
    }

    @Test
    public void getTransporter_ReturnsDto() {
        final var transporter = TransporterFixtureFactory.createTransporter();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(Optional.of(transporter)).when(transporterRepository).findByUuidAndUser(transporter.getUuid(),
                User.withUuid(authenticatedUser.getUuid()));

        final var resultTransporterDto = transporterService.getTransporter(transporter.getUuid(), authenticatedUser);

        assertThat(resultTransporterDto).isEqualTo(TransporterFixtureFactory.createTransporterDto());
        verifyNoMoreInteractions(transporterRepository);
    }

    @Test
    public void updateTransporter_IfNotActive_ThrowsException() {
        final var transporterUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var inactiveTransporter = Transporter.builder().isActive(false).build();
        doReturn(Optional.of(inactiveTransporter)).when(transporterRepository).findByUuidAndUser(transporterUuid, user);

        assertThatExceptionOfType(CannotChangeInactiveEntityException.class).isThrownBy(() -> transporterService
                .updateTransporter(transporterUuid, mock(UpdateTransporterDto.class), authenticatedUser));

        verify(transporterRepository).findByUuidAndUser(transporterUuid, user);
        verifyNoMoreInteractions(transporterRepository);
    }

    @Test
    public void updateTransporter_IfNothingToChange_NoRepositoryCall() {
        final var transporterUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var transporter = TransporterFixtureFactory.createTransporter();
        doReturn(Optional.of(transporter)).when(transporterRepository).findByUuidAndUser(transporterUuid, user);

        final var updateTransporterDto = new UpdateTransporterDto(null, transporter.getCapacity());
        final var expectedUpdateResult = TransporterFixtureFactory.createTransporterDto();
        assertThat(transporterService.updateTransporter(transporterUuid, updateTransporterDto, authenticatedUser))
                .isEqualTo(expectedUpdateResult);

        verify(transporterRepository).findByUuidAndUser(transporterUuid, user);
        verifyNoMoreInteractions(transporterRepository);
    }

    @Test
    public void updateTransporter_IfChangeNeeded_DependenciesCalled() {
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var transporter = TransporterFixtureFactory.createTransporter();
        final var transporterUuid = transporter.getUuid();
        doReturn(Optional.of(transporter)).when(transporterRepository).findByUuidAndUser(transporterUuid, user);

        final var updateTransporterDto = new UpdateTransporterDto("newName", 17);

        final var expectedUpdateResult = transporterService.updateTransporter(
                transporterUuid, updateTransporterDto, authenticatedUser);

        assertThat(expectedUpdateResult.getUuid()).isNotEqualTo(transporterUuid);
        assertThat(expectedUpdateResult.getName()).isEqualTo(updateTransporterDto.getName());
        assertThat(expectedUpdateResult.getCapacity()).isEqualTo(updateTransporterDto.getCapacity());

        verify(transporterRepository).findByUuidAndUser(transporterUuid, user);

        final var transporterCaptor = ArgumentCaptor.forClass(Transporter.class);
        verify(transporterRepository, times(2)).save(transporterCaptor.capture());
        final var savedOldTransporter = transporterCaptor.getAllValues().get(0);
        assertThat(savedOldTransporter.getUuid()).isEqualTo(transporterUuid);
        assertThat(savedOldTransporter.getIsActive()).isFalse();

        final var savedNewTransporter = transporterCaptor.getAllValues().get(1);
        assertThat(savedNewTransporter.getUuid()).isNotEqualTo(transporterUuid);
        assertThat(savedNewTransporter.getName()).isEqualTo(updateTransporterDto.getName());
        assertThat(savedNewTransporter.getCapacity()).isEqualTo(updateTransporterDto.getCapacity());

        verifyNoMoreInteractions(transporterRepository);
    }

    @Test
    public void deleteTransporter_IfNotActive_ThrowsException() {
        final var transporterUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var inactiveTransporter = Transporter.builder().isActive(false).build();
        doReturn(Optional.of(inactiveTransporter)).when(transporterRepository).findByUuidAndUser(transporterUuid, user);

        assertThatExceptionOfType(CannotChangeInactiveEntityException.class).isThrownBy(() -> transporterService
                .deleteTransporter(transporterUuid, authenticatedUser));

        verify(transporterRepository).findByUuidAndUser(transporterUuid, user);
        verifyNoMoreInteractions(transporterRepository);
    }

    @Test
    public void deleteTransporter_IfFound_DependenciesCalled() {
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var transporter = TransporterFixtureFactory.createTransporter();
        final var transporterUuid = transporter.getUuid();
        doReturn(Optional.of(transporter)).when(transporterRepository).findByUuidAndUser(transporterUuid, user);

        transporterService.deleteTransporter(transporterUuid, authenticatedUser);

        verify(transporterRepository).findByUuidAndUser(transporterUuid, user);
        transporter.setIsActive(false);
        verify(transporterRepository).save(transporter);
        verifyNoMoreInteractions(transporterRepository);
    }
}
