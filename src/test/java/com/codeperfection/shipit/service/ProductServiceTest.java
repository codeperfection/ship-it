package com.codeperfection.shipit.service;

import com.codeperfection.shipit.dto.common.PageDto;
import com.codeperfection.shipit.dto.common.PaginationFilterDto;
import com.codeperfection.shipit.dto.product.UpdateCountInStockDto;
import com.codeperfection.shipit.dto.product.UpdateProductDto;
import com.codeperfection.shipit.entity.Product;
import com.codeperfection.shipit.entity.User;
import com.codeperfection.shipit.exception.clienterror.CannotChangeInactiveEntityException;
import com.codeperfection.shipit.exception.clienterror.EntityNotFoundException;
import com.codeperfection.shipit.repository.ProductRepository;
import com.codeperfection.shipit.util.AuthenticationFixtureFactory;
import com.codeperfection.shipit.util.ProductFixtureFactory;
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
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    public void createProduct_IfValidDto_SavesEntity() {
        final var createProductDto = ProductFixtureFactory.createCreateProductDto();
        final var productDto = ProductFixtureFactory.createProductDto();
        final var product = ProductFixtureFactory.createProduct();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(product).when(productRepository).save(any());

        final var savedProductDto = productService.createProduct(createProductDto, authenticatedUser);

        final var productArgumentCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productArgumentCaptor.capture());
        final var savedProduct = productArgumentCaptor.getValue();

        assertThat(savedProduct).usingRecursiveComparison().ignoringFields("user", "uuid", "createdAt")
                .isEqualTo(product);
        assertThat(savedProduct.getCreatedAt()).isCloseToUtcNow(within(10, ChronoUnit.SECONDS));
        assertThat(savedProduct.getUser().getUuid()).isEqualTo(authenticatedUser.getUuid());

        assertThat(savedProductDto).isEqualTo(productDto);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void getProducts_ReturnsPaginatedDtos() {
        final var paginationFilterDto = new PaginationFilterDto(2, 1);
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var databasePage = new PageImpl<>(List.of(ProductFixtureFactory.createProduct()));
        doReturn(databasePage).when(productRepository).findByUserAndIsActiveTrue(
                User.withUuid(authenticatedUser.getUuid()), PageRequest.of(paginationFilterDto.getPage(),
                        paginationFilterDto.getSize(), Sort.by("createdAt")));

        final var productsPage = productService.getProducts(paginationFilterDto, authenticatedUser);

        assertThat(productsPage).isEqualTo(new PageDto<>(databasePage.getTotalElements(),
                databasePage.getTotalPages(), List.of(ProductFixtureFactory.createProductDto())));
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void getProduct_IfNotFound_ThrowsException() {
        final var nonExistingUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        doReturn(Optional.empty()).when(productRepository).findByUuidAndUser(nonExistingUuid, user);

        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() -> productService
                .getProduct(nonExistingUuid, authenticatedUser));

        verify(productRepository).findByUuidAndUser(nonExistingUuid, user);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void getProduct_IfFound_ReturnsDto() {
        final var product = ProductFixtureFactory.createProduct();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(Optional.of(product)).when(productRepository).findByUuidAndUser(product.getUuid(),
                User.withUuid(authenticatedUser.getUuid()));

        final var productDto = productService.getProduct(product.getUuid(), authenticatedUser);

        assertThat(productDto).isEqualTo(ProductFixtureFactory.createProductDto());
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void updateProduct_IfNotFound_ThrowsException() {
        final var nonExistingUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        doReturn(Optional.empty()).when(productRepository).findByUuidAndUser(nonExistingUuid, user);

        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() -> productService
                .updateProduct(nonExistingUuid, mock(UpdateProductDto.class), authenticatedUser));

        verify(productRepository).findByUuidAndUser(nonExistingUuid, user);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void updateProduct_IfNotActive_ThrowsException() {
        final var productUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var inactiveProduct = Product.builder().isActive(false).build();
        doReturn(Optional.of(inactiveProduct)).when(productRepository).findByUuidAndUser(productUuid, user);

        assertThatExceptionOfType(CannotChangeInactiveEntityException.class).isThrownBy(() -> productService
                .updateProduct(productUuid, mock(UpdateProductDto.class), authenticatedUser));

        verify(productRepository).findByUuidAndUser(productUuid, user);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void updateProduct_IfNothingToChange_NoRepositoryCall() {
        final var productUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var product = ProductFixtureFactory.createProduct();
        doReturn(Optional.of(product)).when(productRepository).findByUuidAndUser(productUuid, user);

        final var updateProductDto = new UpdateProductDto(null, null, product.getPrice());
        final var expectedUpdateResult = ProductFixtureFactory.createProductDto();
        assertThat(productService.updateProduct(productUuid, updateProductDto, authenticatedUser))
                .isEqualTo(expectedUpdateResult);

        verify(productRepository).findByUuidAndUser(productUuid, user);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void updateProduct_IfChangeNeeded_DependenciesCalled() {
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var product = ProductFixtureFactory.createProduct();
        final var productUuid = product.getUuid();
        final var countInStock = product.getCountInStock();
        doReturn(Optional.of(product)).when(productRepository).findByUuidAndUser(productUuid, user);

        final var updateProductDto = new UpdateProductDto("newName", 17, 18);

        final var expectedUpdateResult = productService.updateProduct(productUuid, updateProductDto, authenticatedUser);

        assertThat(expectedUpdateResult).usingRecursiveComparison().ignoringFields("uuid", "countInStock")
                .isEqualTo(updateProductDto);
        assertThat(expectedUpdateResult.getUuid()).isNotEqualTo(productUuid);
        assertThat(expectedUpdateResult.getCountInStock()).isEqualTo(countInStock);

        verify(productRepository).findByUuidAndUser(productUuid, user);

        final var productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(2)).save(productCaptor.capture());
        final var savedOldProduct = productCaptor.getAllValues().get(0);
        assertThat(savedOldProduct.getUuid()).isEqualTo(productUuid);
        assertThat(savedOldProduct.getIsActive()).isFalse();
        assertThat(savedOldProduct.getCountInStock()).isZero();

        final var savedNewProduct = productCaptor.getAllValues().get(1);
        assertThat(savedNewProduct.getUuid()).isNotEqualTo(productUuid);
        assertThat(savedNewProduct.getName()).isEqualTo(updateProductDto.getName());
        assertThat(savedNewProduct.getVolume()).isEqualTo(updateProductDto.getVolume());
        assertThat(savedNewProduct.getPrice()).isEqualTo(updateProductDto.getPrice());
        assertThat(savedNewProduct.getCountInStock()).isEqualTo(countInStock);

        verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void updateCountInStock_IfNotFound_ThrowsException() {
        final var nonExistingUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        doReturn(Optional.empty()).when(productRepository).findByUuidAndUser(nonExistingUuid, user);

        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() -> productService
                .updateCountInStock(nonExistingUuid, mock(UpdateCountInStockDto.class), authenticatedUser));

        verify(productRepository).findByUuidAndUser(nonExistingUuid, user);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void updateCountInStock_IfNotActive_ThrowsException() {
        final var productUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var inactiveProduct = Product.builder().isActive(false).build();
        doReturn(Optional.of(inactiveProduct)).when(productRepository).findByUuidAndUser(productUuid, user);

        assertThatExceptionOfType(CannotChangeInactiveEntityException.class).isThrownBy(() -> productService
                .updateCountInStock(productUuid, mock(UpdateCountInStockDto.class), authenticatedUser));

        verify(productRepository).findByUuidAndUser(productUuid, user);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void updateCountInStock_IfNothingToChangeNo_RepositoryCall() {
        final var productUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var product = ProductFixtureFactory.createProduct();
        doReturn(Optional.of(product)).when(productRepository).findByUuidAndUser(productUuid, user);

        final var updateCountInStockDto = new UpdateCountInStockDto(product.getCountInStock());
        final var expectedUpdateResult = ProductFixtureFactory.createProductDto();
        assertThat(productService.updateCountInStock(productUuid, updateCountInStockDto, authenticatedUser))
                .isEqualTo(expectedUpdateResult);

        verify(productRepository).findByUuidAndUser(productUuid, user);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void updateCountInStock_IfChangeNeeded_DependenciesCalled() {
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var product = ProductFixtureFactory.createProduct();
        final var productUuid = product.getUuid();
        doReturn(Optional.of(product)).when(productRepository).findByUuidAndUser(productUuid, user);

        final var updateCountInStockDto = new UpdateCountInStockDto(18);

        final var expectedUpdateResult = productService.updateCountInStock(
                productUuid, updateCountInStockDto, authenticatedUser);

        assertThat(expectedUpdateResult.getUuid()).isEqualTo(productUuid);
        assertThat(expectedUpdateResult.getCountInStock()).isEqualTo(updateCountInStockDto.getCountInStock());

        verify(productRepository).findByUuidAndUser(productUuid, user);

        product.setCountInStock(updateCountInStockDto.getCountInStock());
        verify(productRepository).save(product);

        verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void deleteProduct_IfNotFound_ThrowsException() {
        final var nonExistingUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        doReturn(Optional.empty()).when(productRepository).findByUuidAndUser(nonExistingUuid, user);

        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() -> productService
                .deleteProduct(nonExistingUuid, authenticatedUser));

        verify(productRepository).findByUuidAndUser(nonExistingUuid, user);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void deleteProduct_IfNotActive_ThrowsException() {
        final var productUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var inactiveProduct = Product.builder().isActive(false).build();
        doReturn(Optional.of(inactiveProduct)).when(productRepository).findByUuidAndUser(productUuid, user);

        assertThatExceptionOfType(CannotChangeInactiveEntityException.class).isThrownBy(() -> productService
                .deleteProduct(productUuid, authenticatedUser));

        verify(productRepository).findByUuidAndUser(productUuid, user);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void deleteProduct_IfFound_DependenciesCalled() {
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var product = ProductFixtureFactory.createProduct();
        final var productUuid = product.getUuid();
        doReturn(Optional.of(product)).when(productRepository).findByUuidAndUser(productUuid, user);

        productService.deleteProduct(productUuid, authenticatedUser);

        verify(productRepository).findByUuidAndUser(productUuid, user);
        product.setIsActive(false);
        verify(productRepository).save(product);
        verifyNoMoreInteractions(productRepository);
    }
}
