package com.codeperfection.shipit.service.shipping;

import com.codeperfection.shipit.dto.common.PageDto;
import com.codeperfection.shipit.dto.common.PaginationFilterDto;
import com.codeperfection.shipit.entity.Product;
import com.codeperfection.shipit.entity.User;
import com.codeperfection.shipit.exception.clienterror.EntityNotFoundException;
import com.codeperfection.shipit.repository.ProductRepository;
import com.codeperfection.shipit.repository.ShippingRepository;
import com.codeperfection.shipit.repository.TransporterRepository;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import com.codeperfection.shipit.util.ProductFixtureFactory;
import com.codeperfection.shipit.util.ShippingFixtureFactory;
import com.codeperfection.shipit.util.TransporterFixtureFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
        verifyNoMoreInteractions(shippingHelperComponent, transporterRepository, productRepository, shippingRepository);
    }

    @Test
    public void createShippingIfShippingPossibleReturnsDto() {
        final var createShippingDto = ShippingFixtureFactory.createCreateShippingDto();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var transporter = TransporterFixtureFactory.createTransporter();
        final var user = User.withUuid(authenticatedUser.getUuid());
        doReturn(Optional.of(transporter)).when(transporterRepository).findByUuidAndUser(
                createShippingDto.getTransporterUuid(), user);

        final var product = ProductFixtureFactory.createProduct();
        List<Product> products = List.of(product);
        doReturn(products).when(productRepository).findByUserAndIsActiveTrue(user);
        final var placedProducts = Map.of(product, 3L);
        doReturn(placedProducts).when(shippingHelperComponent).runPlacer(transporter, products);

        final var shipping = ShippingFixtureFactory.createShipping();
        doReturn(shipping.getShippedItems()).when(shippingHelperComponent).createShippedItems(placedProducts);
        doReturn(shipping).when(shippingHelperComponent).saveShipping(createShippingDto, transporter, user,
                shipping.getShippedItems());
        final var shippingDto = ShippingFixtureFactory.createShippingDto();
        doReturn(shippingDto).when(shippingHelperComponent).mapToDto(shipping);

        assertThat(shippingService.createShipping(createShippingDto, authenticatedUser)).isEqualTo(shippingDto);
        verify(shippingHelperComponent).runPlacer(transporter, products);
        verify(shippingHelperComponent).deductPlacedProductsFromStock(placedProducts);
        verifyNoMoreInteractions(shippingHelperComponent, transporterRepository, productRepository, shippingRepository);
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
        verifyNoMoreInteractions(shippingHelperComponent, transporterRepository, productRepository, shippingRepository);
    }

    @Test
    public void getShippingIfNotFoundThrowsException() {
        final var nonExistingUuid = UUID.fromString("613d59d8-2e4a-451a-ac4c-4fc0ac430558");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(Optional.empty()).when(shippingRepository).findByUuidAndUser(nonExistingUuid,
                User.withUuid(authenticatedUser.getUuid()));

        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                shippingService.getShipping(nonExistingUuid, authenticatedUser));
        verifyNoMoreInteractions(shippingHelperComponent, transporterRepository, productRepository, shippingRepository);
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
        verifyNoMoreInteractions(shippingHelperComponent, transporterRepository, productRepository, shippingRepository);
    }
}
