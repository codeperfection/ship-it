package com.codeperfection.shipit.service.shipping;

import com.codeperfection.shipit.dto.common.PageDto;
import com.codeperfection.shipit.dto.common.PaginationFilterDto;
import com.codeperfection.shipit.dto.shipping.CreateShippingDto;
import com.codeperfection.shipit.dto.shipping.ShippingDto;
import com.codeperfection.shipit.entity.Shipping;
import com.codeperfection.shipit.entity.Transporter;
import com.codeperfection.shipit.entity.User;
import com.codeperfection.shipit.exception.clienterror.EntityNotFoundException;
import com.codeperfection.shipit.exception.clienterror.ShippingInactiveTransporterException;
import com.codeperfection.shipit.repository.ProductRepository;
import com.codeperfection.shipit.repository.ShippingRepository;
import com.codeperfection.shipit.repository.TransporterRepository;
import com.codeperfection.shipit.security.AuthenticatedUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShippingService {

    private ShippingHelperComponent shippingHelperComponent;

    private ProductRepository productRepository;

    private TransporterRepository transporterRepository;

    private ShippingRepository shippingRepository;

    public ShippingService(ShippingHelperComponent shippingHelperComponent,
                           ProductRepository productRepository, TransporterRepository transporterRepository,
                           ShippingRepository shippingRepository) {
        this.shippingHelperComponent = shippingHelperComponent;
        this.productRepository = productRepository;
        this.transporterRepository = transporterRepository;
        this.shippingRepository = shippingRepository;
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public ShippingDto createShipping(CreateShippingDto createShippingDto, AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        final Transporter transporter = getActiveTransporter(createShippingDto.getTransporterUuid(), user);
        final var products = productRepository.findByUserAndIsActiveTrue(user);

        final var placedProducts = shippingHelperComponent.runPlacer(transporter, products);
        shippingHelperComponent.deductPlacedProductsFromStock(placedProducts);

        final var shippedItems = shippingHelperComponent.createShippedItems(placedProducts);
        final var shipping = shippingHelperComponent.saveShipping(createShippingDto, transporter, user, shippedItems);
        return shippingHelperComponent.mapToDto(shipping);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public PageDto<ShippingDto> getShippings(PaginationFilterDto paginationFilterDto,
                                             AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var shippingsPage = shippingRepository.findByUser(user,
                PageRequest.of(paginationFilterDto.getPage(), paginationFilterDto.getSize(), Sort.by("createdAt")));

        return PageDto.<ShippingDto>builder()
                .totalPages(shippingsPage.getTotalPages())
                .totalElements(shippingsPage.getTotalElements())
                .elements(shippingsPage.stream().map(shippingHelperComponent::mapToDto).collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public ShippingDto getShipping(UUID uuid, AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        return shippingHelperComponent.mapToDto(getShipping(uuid, user));
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public void deleteShipping(UUID shippingUuid, AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        shippingRepository.delete(getShipping(shippingUuid, user));
    }

    private Transporter getActiveTransporter(UUID transporterUuid, User user) {
        final var transporter = transporterRepository.findByUuidAndUser(transporterUuid, user)
                .orElseThrow(() -> new EntityNotFoundException(transporterUuid));
        if (!transporter.getIsActive()) {
            throw new ShippingInactiveTransporterException(transporterUuid);
        }
        return transporter;
    }

    private Shipping getShipping(UUID uuid, User user) {
        return shippingRepository.findByUuidAndUser(uuid, user)
                .orElseThrow(() -> new EntityNotFoundException(uuid));
    }
}
