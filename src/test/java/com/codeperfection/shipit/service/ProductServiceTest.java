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

    @Mock
    private CommonServiceUtil commonServiceUtil;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    public void createProductIfValidDtoSavesEntity() {
        final var createProductDto = ProductFixtureFactory.createCreateProductDto();
        final var productDto = ProductFixtureFactory.createProductDto();
        final var product = ProductFixtureFactory.createProduct();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(product).when(productRepository).save(any());

        final var savedProductDto = productService.createProduct(createProductDto, authenticatedUser);

        final var productArgumentCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productArgumentCaptor.capture());
        final var savedProduct = productArgumentCaptor.getValue();

        assertThat(savedProduct).isEqualToIgnoringGivenFields(product,
                "user", "uuid", "createdAt");
        assertThat(savedProduct.getCreatedAt()).isCloseToUtcNow(within(10, ChronoUnit.SECONDS));
        assertThat(savedProduct.getUser().getUuid()).isEqualTo(authenticatedUser.getUuid());

        assertThat(savedProductDto).isEqualTo(productDto);
        verifyNoMoreInteractions(productRepository, commonServiceUtil);
    }

    @Test
    public void getProductsReturnsPaginatedDtos() {
        final var paginationFilterDto = new PaginationFilterDto(2, 1);
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var databasePage = new PageImpl<>(List.of(ProductFixtureFactory.createProduct()));
        doReturn(databasePage).when(productRepository).findByUserAndIsActiveTrue(
                User.withUuid(authenticatedUser.getUuid()), PageRequest.of(paginationFilterDto.getPage(),
                        paginationFilterDto.getSize(), Sort.by("createdAt")));

        final var productsPage = productService.getProducts(paginationFilterDto, authenticatedUser);

        assertThat(productsPage).isEqualTo(new PageDto<>(databasePage.getTotalElements(),
                databasePage.getTotalPages(), List.of(ProductFixtureFactory.createProductDto())));
        verifyNoMoreInteractions(productRepository, commonServiceUtil);
    }

    @Test
    public void getProductIfNotFoundThrowsException() {
        final var nonExistingUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        doReturn(Optional.empty()).when(productRepository).findByUuidAndUser(nonExistingUuid, user);

        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() -> productService
                .getProduct(nonExistingUuid, authenticatedUser));

        verify(productRepository).findByUuidAndUser(nonExistingUuid, user);
        verifyNoMoreInteractions(productRepository, commonServiceUtil);
    }

    @Test
    public void getProductIfFoundReturnsDto() {
        final var product = ProductFixtureFactory.createProduct();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(Optional.of(product)).when(productRepository).findByUuidAndUser(product.getUuid(),
                User.withUuid(authenticatedUser.getUuid()));

        final var productDto = productService.getProduct(product.getUuid(), authenticatedUser);

        assertThat(productDto).isEqualTo(ProductFixtureFactory.createProductDto());
        verifyNoMoreInteractions(productRepository, commonServiceUtil);
    }

    @Test
    public void updateProductIfNotFoundThrowsException() {
        final var nonExistingUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        doReturn(Optional.empty()).when(productRepository).findByUuidAndUser(nonExistingUuid, user);

        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() -> productService
                .updateProduct(nonExistingUuid, mock(UpdateProductDto.class), authenticatedUser));

        verify(productRepository).findByUuidAndUser(nonExistingUuid, user);
        verifyNoMoreInteractions(productRepository, commonServiceUtil);
    }

    @Test
    public void updateProductIfNotActiveThrowsException() {
        final var productUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var inactiveProduct = Product.builder().isActive(false).build();
        doReturn(Optional.of(inactiveProduct)).when(productRepository).findByUuidAndUser(productUuid, user);

        assertThatExceptionOfType(CannotChangeInactiveEntityException.class).isThrownBy(() -> productService
                .updateProduct(productUuid, mock(UpdateProductDto.class), authenticatedUser));

        verify(productRepository).findByUuidAndUser(productUuid, user);
        verifyNoMoreInteractions(productRepository, commonServiceUtil);
    }

    @Test
    public void updateProductIfNothingToChangeNoRepositoryCall() {
        final var productUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var product = ProductFixtureFactory.createProduct();
        doReturn(Optional.of(product)).when(productRepository).findByUuidAndUser(productUuid, user);

        when(commonServiceUtil.applyChangeIfNeeded(any(), any(), any())).thenCallRealMethod();
        final var updateProductDto = new UpdateProductDto(null, null, product.getPrice());
        final var expectedUpdateResult = ProductFixtureFactory.createProductDto();
        assertThat(productService.updateProduct(productUuid, updateProductDto, authenticatedUser))
                .isEqualTo(expectedUpdateResult);

        verify(productRepository).findByUuidAndUser(productUuid, user);
        verify(commonServiceUtil, times(3)).applyChangeIfNeeded(any(), any(), any());
        verifyNoMoreInteractions(productRepository, commonServiceUtil);
    }

    @Test
    public void updateProductIfChangeNeededDependenciesCalled() {
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var product = ProductFixtureFactory.createProduct();
        final var productUuid = product.getUuid();
        final var countInStock = product.getCountInStock();
        doReturn(Optional.of(product)).when(productRepository).findByUuidAndUser(productUuid, user);

        when(commonServiceUtil.applyChangeIfNeeded(any(), any(), any())).thenCallRealMethod();
        final var updateProductDto = new UpdateProductDto("newName", 17, 18);

        final var expectedUpdateResult = productService.updateProduct(productUuid, updateProductDto, authenticatedUser);

        assertThat(expectedUpdateResult.getUuid()).isNotEqualTo(productUuid);
        assertThat(expectedUpdateResult.getName()).isEqualTo(updateProductDto.getName());
        assertThat(expectedUpdateResult.getVolume()).isEqualTo(updateProductDto.getVolume());
        assertThat(expectedUpdateResult.getPrice()).isEqualTo(updateProductDto.getPrice());
        assertThat(expectedUpdateResult.getCountInStock()).isEqualTo(countInStock);

        verify(productRepository).findByUuidAndUser(productUuid, user);
        verify(commonServiceUtil, times(3)).applyChangeIfNeeded(any(), any(), any());

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

        verifyNoMoreInteractions(productRepository, commonServiceUtil);
    }

    @Test
    public void updateCountInStockIfNotFoundThrowsException() {
        final var nonExistingUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        doReturn(Optional.empty()).when(productRepository).findByUuidAndUser(nonExistingUuid, user);

        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() -> productService
                .updateCountInStock(nonExistingUuid, mock(UpdateCountInStockDto.class), authenticatedUser));

        verify(productRepository).findByUuidAndUser(nonExistingUuid, user);
        verifyNoMoreInteractions(productRepository, commonServiceUtil);
    }

    @Test
    public void updateCountInStockIfNotActiveThrowsException() {
        final var productUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var inactiveProduct = Product.builder().isActive(false).build();
        doReturn(Optional.of(inactiveProduct)).when(productRepository).findByUuidAndUser(productUuid, user);

        assertThatExceptionOfType(CannotChangeInactiveEntityException.class).isThrownBy(() -> productService
                .updateCountInStock(productUuid, mock(UpdateCountInStockDto.class), authenticatedUser));

        verify(productRepository).findByUuidAndUser(productUuid, user);
        verifyNoMoreInteractions(productRepository, commonServiceUtil);
    }

    @Test
    public void updateCountInStockIfNothingToChangeNoRepositoryCall() {
        final var productUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var product = ProductFixtureFactory.createProduct();
        doReturn(Optional.of(product)).when(productRepository).findByUuidAndUser(productUuid, user);

        when(commonServiceUtil.applyChangeIfNeeded(any(), any(), any())).thenCallRealMethod();
        final var updateCountInStockDto = new UpdateCountInStockDto(product.getCountInStock());
        final var expectedUpdateResult = ProductFixtureFactory.createProductDto();
        assertThat(productService.updateCountInStock(productUuid, updateCountInStockDto, authenticatedUser))
                .isEqualTo(expectedUpdateResult);

        verify(commonServiceUtil).applyChangeIfNeeded(any(), any(), any());
        verify(productRepository).findByUuidAndUser(productUuid, user);
        verifyNoMoreInteractions(productRepository, commonServiceUtil);
    }

    @Test
    public void updateCountInStockIfChangeNeededDependenciesCalled() {
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var product = ProductFixtureFactory.createProduct();
        final var productUuid = product.getUuid();
        doReturn(Optional.of(product)).when(productRepository).findByUuidAndUser(productUuid, user);

        when(commonServiceUtil.applyChangeIfNeeded(any(), any(), any())).thenCallRealMethod();
        final var updateCountInStockDto = new UpdateCountInStockDto(18);

        final var expectedUpdateResult = productService.updateCountInStock(
                productUuid, updateCountInStockDto, authenticatedUser);

        assertThat(expectedUpdateResult.getUuid()).isEqualTo(productUuid);
        assertThat(expectedUpdateResult.getCountInStock()).isEqualTo(updateCountInStockDto.getCountInStock());

        verify(productRepository).findByUuidAndUser(productUuid, user);
        verify(commonServiceUtil, times(1)).applyChangeIfNeeded(any(), any(), any());

        product.setCountInStock(updateCountInStockDto.getCountInStock());
        verify(productRepository).save(product);

        verifyNoMoreInteractions(productRepository, commonServiceUtil);
    }

    @Test
    public void deleteProductIfNotFoundThrowsException() {
        final var nonExistingUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        doReturn(Optional.empty()).when(productRepository).findByUuidAndUser(nonExistingUuid, user);

        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() -> productService
                .deleteProduct(nonExistingUuid, authenticatedUser));

        verify(productRepository).findByUuidAndUser(nonExistingUuid, user);
        verifyNoMoreInteractions(productRepository, commonServiceUtil);
    }

    @Test
    public void deleteProductIfNotActiveThrowsException() {
        final var productUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var inactiveProduct = Product.builder().isActive(false).build();
        doReturn(Optional.of(inactiveProduct)).when(productRepository).findByUuidAndUser(productUuid, user);

        assertThatExceptionOfType(CannotChangeInactiveEntityException.class).isThrownBy(() -> productService
                .deleteProduct(productUuid, authenticatedUser));

        verify(productRepository).findByUuidAndUser(productUuid, user);
        verifyNoMoreInteractions(productRepository, commonServiceUtil);
    }

    @Test
    public void deleteProductIfFoundDependenciesCalled() {
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var product = ProductFixtureFactory.createProduct();
        final var productUuid = product.getUuid();
        doReturn(Optional.of(product)).when(productRepository).findByUuidAndUser(productUuid, user);

        productService.deleteProduct(productUuid, authenticatedUser);

        verify(productRepository).findByUuidAndUser(productUuid, user);
        product.setIsActive(false);
        verify(productRepository).save(product);
        verifyNoMoreInteractions(productRepository, commonServiceUtil);
    }
}
