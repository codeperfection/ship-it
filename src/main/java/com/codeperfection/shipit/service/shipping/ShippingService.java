package com.codeperfection.shipit.service.shipping;

import com.codeperfection.shipit.dto.common.PageDto;
import com.codeperfection.shipit.dto.common.PaginationFilterDto;
import com.codeperfection.shipit.dto.shipping.CreateShippingDto;
import com.codeperfection.shipit.dto.shipping.ShippingDto;
import com.codeperfection.shipit.entity.Product;
import com.codeperfection.shipit.entity.User;
import com.codeperfection.shipit.exception.clienterror.EntityNotFoundException;
import com.codeperfection.shipit.exception.clienterror.ShippingImpossibleException;
import com.codeperfection.shipit.repository.ProductRepository;
import com.codeperfection.shipit.repository.ShippingRepository;
import com.codeperfection.shipit.repository.TransporterRepository;
import com.codeperfection.shipit.security.AuthenticatedUser;
import com.codeperfection.shipit.service.shipping.placer.Item;
import com.codeperfection.shipit.service.shipping.placer.KnapsackPlacer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShippingService {

    private ShippingHelperComponent shippingHelperComponent;

    private TransporterRepository transporterRepository;

    private ProductRepository productRepository;

    private ShippingRepository shippingRepository;

    private KnapsackPlacer knapsackPlacer;

    public ShippingService(ShippingHelperComponent shippingHelperComponent, TransporterRepository transporterRepository,
                           ProductRepository productRepository, ShippingRepository shippingRepository,
                           KnapsackPlacer knapsackPlacer) {
        this.shippingHelperComponent = shippingHelperComponent;
        this.transporterRepository = transporterRepository;
        this.productRepository = productRepository;
        this.shippingRepository = shippingRepository;
        this.knapsackPlacer = knapsackPlacer;
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public ShippingDto createShipping(CreateShippingDto createShippingDto, AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var transporter = transporterRepository.findByUuidAndUser(createShippingDto.getTransporterUuid(), user)
                .orElseThrow(() -> new EntityNotFoundException(createShippingDto.getTransporterUuid()));
        final var items = shippingHelperComponent.convertToItems(productRepository.findByUserAndIsActiveTrue(user));

        final var productToCount = knapsackPlacer.place(items, transporter.getCapacity()).getItems().stream()
                .collect(Collectors.groupingBy(Item::getProduct,
                        () -> new TreeMap<>(Comparator.comparing(Product::getUuid)), Collectors.counting()));
        if (productToCount.isEmpty()) {
            throw new ShippingImpossibleException();
        }

        shippingHelperComponent.deductPlacedProductsFromStock(productToCount);
        final var shippedItems = shippingHelperComponent.createShippedItems(productToCount);
        final var shipping = shippingHelperComponent.saveShipping(createShippingDto, transporter, user, shippedItems);
        return shippingHelperComponent.mapToDto(shipping);
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
