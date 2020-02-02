package com.codeperfection.shipit.service;

import com.codeperfection.shipit.dto.PageDto;
import com.codeperfection.shipit.dto.PaginationFilterDto;
import com.codeperfection.shipit.dto.TransporterDto;
import com.codeperfection.shipit.entity.Transporter;
import com.codeperfection.shipit.entity.User;
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

    private TransporterRepository transporterRepository;

    private ModelMapper modelMapper;

    public TransporterService(TransporterRepository transporterRepository, ModelMapper modelMapper) {
        this.transporterRepository = transporterRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public TransporterDto save(TransporterDto transporterDto, AuthenticatedUser authenticatedUser) {
        final var transporter = transporterRepository.save(createTransporter(transporterDto,
                authenticatedUser.getUuid()));
        return modelMapper.map(transporter, TransporterDto.class);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public PageDto<TransporterDto> getTransporters(PaginationFilterDto paginationFilterDto,
                                                   AuthenticatedUser authenticatedUser) {
        final var transportersPage = transporterRepository.findByUserAndIsActiveTrue(
                User.withUuid(authenticatedUser.getUuid()), PageRequest.of(paginationFilterDto.getPage(),
                        paginationFilterDto.getSize(), Sort.by("createdAt")));

        return PageDto.<TransporterDto>builder()
                .totalPages(transportersPage.getTotalPages())
                .totalElements(transportersPage.getTotalElements())
                .elements(transportersPage.stream().map(transporter ->
                        modelMapper.map(transporter, TransporterDto.class)).collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public TransporterDto getTransporter(UUID uuid, AuthenticatedUser authenticatedUser) {
        final var transporter = transporterRepository.findByUuidAndUser(uuid, User.withUuid(authenticatedUser.getUuid()))
                .orElseThrow(() -> new EntityNotFoundException(uuid));
        return modelMapper.map(transporter, TransporterDto.class);
    }

    private Transporter createTransporter(TransporterDto transporterDto, UUID userUuid) {
        return Transporter.builder()
                .uuid(UUID.randomUUID())
                .name(transporterDto.getName())
                .capacity(transporterDto.getCapacity())
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .user(User.withUuid(userUuid))
                .build();
    }
}
