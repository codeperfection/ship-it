package com.codeperfection.shipit.service;

import com.codeperfection.shipit.dto.PageDto;
import com.codeperfection.shipit.dto.PaginationFilterDto;
import com.codeperfection.shipit.entity.Transporter;
import com.codeperfection.shipit.entity.User;
import com.codeperfection.shipit.exception.clienterror.EntityNotFoundException;
import com.codeperfection.shipit.repository.TransporterRepository;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import com.codeperfection.shipit.util.ShippingFixtureFactory;
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
    public void saveIfValidDtoSavesEntity() {
        final var transporterDto = ShippingFixtureFactory.createTransporterDto();
        final var transporter = ShippingFixtureFactory.createTransporter();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(transporter).when(transporterRepository).save(any());

        final var savedTransporterDto = transporterService.save(transporterDto, authenticatedUser);

        final var transporterArgumentCaptor = ArgumentCaptor.forClass(Transporter.class);
        verify(transporterRepository).save(transporterArgumentCaptor.capture());
        final var savedTransporter = transporterArgumentCaptor.getValue();

        assertThat(savedTransporter).isEqualToIgnoringGivenFields(transporter,
                "user", "uuid", "createdAt");
        assertThat(savedTransporter.getCreatedAt()).isCloseToUtcNow(within(10, ChronoUnit.SECONDS));
        assertThat(savedTransporter.getUser().getUuid()).isEqualTo(authenticatedUser.getUuid());

        assertThat(savedTransporterDto).isEqualTo(transporterDto);
        verifyNoMoreInteractions(transporterRepository);
    }

    @Test
    public void getTransportersReturnsPaginatedDtos() {
        final var paginationFilterDto = new PaginationFilterDto(2, 1);
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var databasePage = new PageImpl<>(List.of(ShippingFixtureFactory.createTransporter()));
        doReturn(databasePage).when(transporterRepository).findByUserAndIsActiveTrue(
                User.withUuid(authenticatedUser.getUuid()), PageRequest.of(paginationFilterDto.getPage(),
                        paginationFilterDto.getSize(), Sort.by("createdAt")));

        final var transportersPage = transporterService.getTransporters(paginationFilterDto, authenticatedUser);

        assertThat(transportersPage).isEqualTo(new PageDto<>(databasePage.getTotalElements(),
                databasePage.getTotalPages(), List.of(ShippingFixtureFactory.createTransporterDto())));
        verifyNoMoreInteractions(transporterRepository);
    }

    @Test
    public void getTransporterIfNotFoundThrowsException() {
        final var nonExistingUuid = UUID.fromString("bdc52538-b488-4f67-a6c4-4f4116b5174e");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(Optional.empty()).when(transporterRepository).findByUuidAndUser(nonExistingUuid,
                User.withUuid(authenticatedUser.getUuid()));

        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                transporterService.getTransporter(nonExistingUuid, authenticatedUser));
        verifyNoMoreInteractions(transporterRepository);
    }

    @Test
    public void getTransporterIfFoundReturnsDto() {
        final var transporter = ShippingFixtureFactory.createTransporter();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(Optional.of(transporter)).when(transporterRepository).findByUuidAndUser(transporter.getUuid(),
                User.withUuid(authenticatedUser.getUuid()));

        final var transporterDto = transporterService.getTransporter(transporter.getUuid(), authenticatedUser);

        assertThat(transporterDto).isEqualTo(ShippingFixtureFactory.createTransporterDto());
        verifyNoMoreInteractions(transporterRepository);
    }
}
