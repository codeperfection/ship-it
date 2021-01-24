package com.codeperfection.shipit.service;

import com.codeperfection.shipit.dto.common.PageDto;
import com.codeperfection.shipit.dto.common.PaginationFilterDto;
import com.codeperfection.shipit.dto.transporter.CreateTransporterDto;
import com.codeperfection.shipit.dto.transporter.TransporterDto;
import com.codeperfection.shipit.dto.transporter.UpdateTransporterDto;
import com.codeperfection.shipit.entity.Transporter;
import com.codeperfection.shipit.entity.User;
import com.codeperfection.shipit.exception.clienterror.CannotChangeInactiveEntityException;
import com.codeperfection.shipit.exception.clienterror.EntityNotFoundException;
import com.codeperfection.shipit.repository.TransporterRepository;
import com.codeperfection.shipit.security.AuthenticatedUser;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransporterService {

    private final TransporterRepository transporterRepository;

    private final ModelMapper modelMapper;

    public TransporterService(TransporterRepository transporterRepository, ModelMapper modelMapper) {
        this.transporterRepository = transporterRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public TransporterDto createTransporter(CreateTransporterDto createTransporterDto,
                                            AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var transporter = transporterRepository.save(createTransporter(createTransporterDto, user));
        return mapToDto(transporter);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public PageDto<TransporterDto> getTransporters(PaginationFilterDto paginationFilterDto,
                                                   AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var transportersPage = transporterRepository.findByUserAndIsActiveTrue(user,
                PageRequest.of(paginationFilterDto.getPage(), paginationFilterDto.getSize(), Sort.by("createdAt")));

        return PageDto.<TransporterDto>builder()
                .totalPages(transportersPage.getTotalPages())
                .totalElements(transportersPage.getTotalElements())
                .elements(transportersPage.stream().map(this::mapToDto).collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public TransporterDto getTransporter(UUID transporterUuid, AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        return mapToDto(getTransporter(transporterUuid, user));
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public TransporterDto updateTransporter(UUID transporterUuid, UpdateTransporterDto updateTransporterDto,
                                            AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var currentTransporter = getActiveTransporterForUpdate(transporterUuid, user);

        final var newTransporter = createNewVersion(currentTransporter, user);
        if (applyChanges(newTransporter, updateTransporterDto)) {
            deactivate(currentTransporter);
            transporterRepository.save(newTransporter);
            return mapToDto(newTransporter);
        }

        return mapToDto(currentTransporter);
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public void deleteTransporter(UUID transporterUuid, AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        deactivate(getActiveTransporterForUpdate(transporterUuid, user));
    }

    private Transporter createTransporter(CreateTransporterDto createTransporterDto, User user) {
        return Transporter.builder()
                .uuid(UUID.randomUUID())
                .name(createTransporterDto.getName())
                .capacity(createTransporterDto.getCapacity())
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .user(user)
                .build();
    }

    private TransporterDto mapToDto(Transporter transporter) {
        return modelMapper.map(transporter, TransporterDto.class);
    }

    private Transporter getActiveTransporterForUpdate(UUID transporterUuid, User user) {
        final var transporter = getTransporter(transporterUuid, user);
        if (!transporter.getIsActive()) {
            throw new CannotChangeInactiveEntityException(transporterUuid);
        }
        return transporter;
    }

    private Transporter createNewVersion(Transporter currentTransporter, User user) {
        return Transporter.builder()
                .uuid(UUID.randomUUID())
                .name(currentTransporter.getName())
                .capacity(currentTransporter.getCapacity())
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .user(user)
                .build();
    }

    private boolean applyChanges(Transporter transporter, UpdateTransporterDto updateDto) {
        boolean changed = CommonServiceUtil.applyChangeIfNeeded(
                transporter.getName(), updateDto.getName(), transporter::setName);
        changed |= CommonServiceUtil.applyChangeIfNeeded(
                transporter.getCapacity(), updateDto.getCapacity(), transporter::setCapacity);

        return changed;
    }

    private void deactivate(Transporter currentTransporter) {
        currentTransporter.setIsActive(false);
        transporterRepository.save(currentTransporter);
    }

    private Transporter getTransporter(UUID transporterUuid, User user) {
        return transporterRepository.findByUuidAndUser(transporterUuid, user)
                .orElseThrow(() -> new EntityNotFoundException(transporterUuid));
    }
}
