package com.codeperfection.shipit.service;

import com.codeperfection.shipit.dto.common.PageDto;
import com.codeperfection.shipit.dto.common.PaginationFilterDto;
import com.codeperfection.shipit.entity.Transporter;
import com.codeperfection.shipit.entity.User;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransporterServiceTest {

    @Mock
    private TransporterRepository transporterRepository;

    @Mock
    private CommonServiceUtil commonServiceUtil;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private TransporterService transporterService;

    @Test
    public void createTransporterIfValidDtoSavesEntity() {
        final var createTransporterDto = TransporterFixtureFactory.createCreateTransporterDto();
        final var transporterDto = TransporterFixtureFactory.createTransporterDto();
        final var transporter = TransporterFixtureFactory.createTransporter();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(transporter).when(transporterRepository).save(any());

        final var savedTransporterDto = transporterService.createTransporter(createTransporterDto, authenticatedUser);

        final var transporterArgumentCaptor = ArgumentCaptor.forClass(Transporter.class);
        verify(transporterRepository).save(transporterArgumentCaptor.capture());
        final var savedTransporter = transporterArgumentCaptor.getValue();

        assertThat(savedTransporter).isEqualToIgnoringGivenFields(transporter,
                "user", "uuid", "createdAt");
        assertThat(savedTransporter.getCreatedAt()).isCloseToUtcNow(within(10, ChronoUnit.SECONDS));
        assertThat(savedTransporter.getUser().getUuid()).isEqualTo(authenticatedUser.getUuid());

        assertThat(savedTransporterDto).isEqualTo(transporterDto);
        verifyNoMoreInteractions(transporterRepository, commonServiceUtil);
    }

    @Test
    public void getTransportersReturnsPaginatedDtos() {
        final var paginationFilterDto = new PaginationFilterDto(2, 1);
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var databasePage = new PageImpl<>(List.of(TransporterFixtureFactory.createTransporter()));
        doReturn(databasePage).when(transporterRepository).findByUserAndIsActiveTrue(
                User.withUuid(authenticatedUser.getUuid()), PageRequest.of(paginationFilterDto.getPage(),
                        paginationFilterDto.getSize(), Sort.by("createdAt")));

        final var transportersPage = transporterService.getTransporters(paginationFilterDto, authenticatedUser);

        assertThat(transportersPage).isEqualTo(new PageDto<>(databasePage.getTotalElements(),
                databasePage.getTotalPages(), List.of(TransporterFixtureFactory.createTransporterDto())));
        verifyNoMoreInteractions(transporterRepository, commonServiceUtil);
    }

    @Test
    public void getTransporterReturnsDto() {
        final var transporter = TransporterFixtureFactory.createTransporter();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(transporter).when(commonServiceUtil).getTransporter(transporter.getUuid(),
                User.withUuid(authenticatedUser.getUuid()));

        final var resultTransporterDto = transporterService.getTransporter(transporter.getUuid(), authenticatedUser);

        assertThat(resultTransporterDto).isEqualTo(TransporterFixtureFactory.createTransporterDto());
        verifyNoMoreInteractions(transporterRepository, commonServiceUtil);
    }
}
