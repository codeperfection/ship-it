package com.codeperfection.shipit.service.shipping;

import com.codeperfection.shipit.dto.common.PageDto;
import com.codeperfection.shipit.dto.common.PaginationFilterDto;
import com.codeperfection.shipit.dto.shipping.CreateShippingDto;
import com.codeperfection.shipit.dto.shipping.ShippingDto;
import com.codeperfection.shipit.entity.Transporter;
import com.codeperfection.shipit.entity.User;
import com.codeperfection.shipit.exception.clienterror.EntityNotFoundException;
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

    private TransporterRepository transporterRepository;

    private ProductRepository productRepository;

    private ShippingRepository shippingRepository;

    public ShippingService(ShippingHelperComponent shippingHelperComponent, TransporterRepository transporterRepository,
                           ProductRepository productRepository, ShippingRepository shippingRepository) {
        this.shippingHelperComponent = shippingHelperComponent;
        this.transporterRepository = transporterRepository;
        this.productRepository = productRepository;
        this.shippingRepository = shippingRepository;
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public ShippingDto createShipping(CreateShippingDto createShippingDto, AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var transporter = getTransporter(createShippingDto, user);
        final var products = productRepository.findByUserAndIsActiveTrue(user);

        final var placedProducts = shippingHelperComponent.runPlacer(transporter, products);
        shippingHelperComponent.deductPlacedProductsFromStock(placedProducts);

        final var shippedItems = shippingHelperComponent.createShippedItems(placedProducts);
        final var shipping = shippingHelperComponent.saveShipping(createShippingDto, transporter, user, shippedItems);
        return shippingHelperComponent.mapToDto(shipping);
    }

    private Transporter getTransporter(CreateShippingDto createShippingDto, User user) {
        final var transporterUuid = createShippingDto.getTransporterUuid();
        return transporterRepository.findByUuidAndUser(transporterUuid, user)
                .orElseThrow(() -> new EntityNotFoundException(transporterUuid));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public PageDto<ShippingDto> getShippings(PaginationFilterDto paginationFilterDto,
                                             AuthenticatedUser authenticatedUser) {
        final var shippingsPage = shippingRepository.findByUser(User.withUuid(authenticatedUser.getUuid()),
                PageRequest.of(paginationFilterDto.getPage(), paginationFilterDto.getSize(), Sort.by("createdAt")));

        return PageDto.<ShippingDto>builder()
                .totalPages(shippingsPage.getTotalPages())
                .totalElements(shippingsPage.getTotalElements())
                .elements(shippingsPage.stream().map(shipping -> shippingHelperComponent.mapToDto(shipping))
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public ShippingDto getShipping(UUID uuid, AuthenticatedUser authenticatedUser) {
        final var shipping = shippingRepository.findByUuidAndUser(uuid, User.withUuid(authenticatedUser.getUuid()))
                .orElseThrow(() -> new EntityNotFoundException(uuid));
        return shippingHelperComponent.mapToDto(shipping);
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public void deleteShipping(UUID shippingUuid, AuthenticatedUser authenticatedUser) {
        // TODO: fill body
    }
}
