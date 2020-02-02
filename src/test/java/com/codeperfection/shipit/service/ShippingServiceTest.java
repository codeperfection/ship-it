package com.codeperfection.shipit.service;

import com.codeperfection.shipit.dto.PageDto;
import com.codeperfection.shipit.dto.PaginationFilterDto;
import com.codeperfection.shipit.entity.User;
import com.codeperfection.shipit.exception.clienterror.EntityNotFoundException;
import com.codeperfection.shipit.exception.clienterror.ShippingImpossibleException;
import com.codeperfection.shipit.placer.Knapsack;
import com.codeperfection.shipit.placer.KnapsackPlacer;
import com.codeperfection.shipit.repository.ProductRepository;
import com.codeperfection.shipit.repository.ShippingRepository;
import com.codeperfection.shipit.repository.TransporterRepository;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import com.codeperfection.shipit.util.ProductFixtureFactory;
import com.codeperfection.shipit.util.ShippingFixtureFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShippingServiceTest {

    @Mock
    private ShippingHelperComponent shippingHelperComponent;

    @Mock
    private TransporterRepository transporterRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ShippingRepository shippingRepository;

    @Mock
    private KnapsackPlacer knapsackPlacer;

    @InjectMocks
    private ShippingService shippingService;

    @Test
    public void createShippingIfInvalidTransporterThrowsException() {
        final var createShippingDto = ShippingFixtureFactory.createCreateShippingDto();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(Optional.empty()).when(transporterRepository).findByUuidAndUser(createShippingDto.getTransporterUuid(),
                User.withUuid(authenticatedUser.getUuid()));
        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() -> shippingService.createShipping(
                createShippingDto, authenticatedUser));
        verifyNoMoreInteractions(shippingHelperComponent, transporterRepository, productRepository, knapsackPlacer);
    }

    @Test
    public void createShippingIfShippingImpossibleThrowsException() {
        final var createShippingDto = ShippingFixtureFactory.createCreateShippingDto();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var transporter = ShippingFixtureFactory.createTransporter();
        final var user = User.withUuid(authenticatedUser.getUuid());
        doReturn(Optional.of(transporter)).when(transporterRepository).findByUuidAndUser(
                createShippingDto.getTransporterUuid(), user);

        final var product = ProductFixtureFactory.createProduct();
        final var knapsackItems = ProductFixtureFactory.createKnapsackItems();
        doReturn(List.of(product)).when(productRepository).findByUserAndIsActiveTrue(user);
        doReturn(knapsackItems).when(shippingHelperComponent).convertToItems(List.of(product));
        final var emptyKnapsack = new Knapsack(0, 0, Collections.emptyList());
        doReturn(emptyKnapsack).when(knapsackPlacer).place(knapsackItems, transporter.getCapacity());

        assertThatExceptionOfType(ShippingImpossibleException.class).isThrownBy(() -> shippingService.createShipping(
                createShippingDto, authenticatedUser));
        verifyNoMoreInteractions(shippingHelperComponent, transporterRepository, productRepository, knapsackPlacer);
    }

    @Test
    public void createShippingIfShippingPossibleReturnsDto() {
        final var createShippingDto = ShippingFixtureFactory.createCreateShippingDto();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var transporter = ShippingFixtureFactory.createTransporter();
        final var user = User.withUuid(authenticatedUser.getUuid());
        doReturn(Optional.of(transporter)).when(transporterRepository).findByUuidAndUser(
                createShippingDto.getTransporterUuid(), user);

        final var product = ProductFixtureFactory.createProduct();
        final var knapsackItems = ProductFixtureFactory.createKnapsackItems();
        doReturn(List.of(product)).when(productRepository).findByUserAndIsActiveTrue(user);
        doReturn(knapsackItems).when(shippingHelperComponent).convertToItems(List.of(product));
        final var knapsack = ProductFixtureFactory.crateKnapsack();
        doReturn(knapsack).when(knapsackPlacer).place(knapsackItems, transporter.getCapacity());

        final var productToCount = Map.of(product, 2L);
        doNothing().when(shippingHelperComponent).deductPlacedProductsFromStock(productToCount);
        final var shipping = ShippingFixtureFactory.createShipping();
        doReturn(shipping.getShippedItems()).when(shippingHelperComponent).createShippedItems(productToCount);
        doReturn(shipping).when(shippingHelperComponent).saveShipping(createShippingDto, transporter, user,
                shipping.getShippedItems());
        final var shippingDto = ShippingFixtureFactory.createShippingDto();
        doReturn(shippingDto).when(shippingHelperComponent).mapToDto(shipping);

        assertThat(shippingService.createShipping(createShippingDto, authenticatedUser)).isEqualTo(shippingDto);

        verifyNoMoreInteractions(shippingHelperComponent, transporterRepository, productRepository, knapsackPlacer);
    }

    @Test
    public void getShippingsReturnsPaginatedDtos() {
        final var paginationFilterDto = new PaginationFilterDto(2, 1);
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var shipping = ShippingFixtureFactory.createShipping();
        final var databasePage = new PageImpl<>(List.of(shipping));
        doReturn(databasePage).when(shippingRepository).findByUser(User.withUuid(authenticatedUser.getUuid()),
                PageRequest.of(paginationFilterDto.getPage(), paginationFilterDto.getSize(), Sort.by("createdAt")));
        final var shippingDto = ShippingFixtureFactory.createShippingDto();
        doReturn(shippingDto).when(shippingHelperComponent).mapToDto(shipping);

        final var shippingsPage = shippingService.getShippings(paginationFilterDto, authenticatedUser);

        assertThat(shippingsPage).isEqualTo(new PageDto<>(databasePage.getTotalElements(),
                databasePage.getTotalPages(), List.of(shippingDto)));
        verifyNoMoreInteractions(shippingRepository, shippingHelperComponent);
    }

    @Test
    public void getShippingIfNotFoundThrowsException() {
        final var nonExistingUuid = UUID.fromString("613d59d8-2e4a-451a-ac4c-4fc0ac430558");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(Optional.empty()).when(shippingRepository).findByUuidAndUser(nonExistingUuid,
                User.withUuid(authenticatedUser.getUuid()));

        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                shippingService.getShipping(nonExistingUuid, authenticatedUser));
        verifyNoMoreInteractions(shippingRepository, shippingHelperComponent);
    }

    @Test
    public void getShippingIfFoundReturnsDto() {
        final var shipping = ShippingFixtureFactory.createShipping();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(Optional.of(shipping)).when(shippingRepository).findByUuidAndUser(shipping.getUuid(),
                User.withUuid(authenticatedUser.getUuid()));
        final var expectedShippingDto = ShippingFixtureFactory.createShippingDto();
        doReturn(expectedShippingDto).when(shippingHelperComponent).mapToDto(shipping);

        final var shippingDto = shippingService.getShipping(shipping.getUuid(), authenticatedUser);

        assertThat(shippingDto).isEqualTo(expectedShippingDto);
        verifyNoMoreInteractions(shippingRepository, shippingHelperComponent);
    }
}
