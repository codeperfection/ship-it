package com.codeperfection.shipit.service;

import com.codeperfection.shipit.dto.common.PageDto;
import com.codeperfection.shipit.dto.common.PaginationFilterDto;
import com.codeperfection.shipit.entity.Product;
import com.codeperfection.shipit.entity.User;
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
    public void saveIfValidDtoSavesEntity() {
        final var createProductDto = ProductFixtureFactory.createCreateProductDto();
        final var productDto = ProductFixtureFactory.createProductDto();
        final var product = ProductFixtureFactory.createProduct();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(product).when(productRepository).save(any());

        final var savedProductDto = productService.save(createProductDto, authenticatedUser);

        final var productArgumentCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productArgumentCaptor.capture());
        final var savedProduct = productArgumentCaptor.getValue();

        assertThat(savedProduct).isEqualToIgnoringGivenFields(product,
                "user", "uuid", "createdAt");
        assertThat(savedProduct.getCreatedAt()).isCloseToUtcNow(within(10, ChronoUnit.SECONDS));
        assertThat(savedProduct.getUser().getUuid()).isEqualTo(authenticatedUser.getUuid());

        assertThat(savedProductDto).isEqualTo(productDto);
        verifyNoMoreInteractions(productRepository);
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
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void getProductIfNotFoundThrowsException() {
        final var nonExistingUuid = UUID.fromString("1f732731-6f3e-4f81-a727-d025c376dcad");
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(Optional.empty()).when(productRepository).findByUuidAndUser(nonExistingUuid,
                User.withUuid(authenticatedUser.getUuid()));

        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() ->
                productService.getProduct(nonExistingUuid, authenticatedUser));
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void getProductIfFoundReturnsDto() {
        final var product = ProductFixtureFactory.createProduct();
        final var authenticatedUser = AuthenticationFixtureFactory.createAuthenticatedUser();
        doReturn(Optional.of(product)).when(productRepository).findByUuidAndUser(product.getUuid(),
                User.withUuid(authenticatedUser.getUuid()));

        final var productDto = productService.getProduct(product.getUuid(), authenticatedUser);

        assertThat(productDto).isEqualTo(ProductFixtureFactory.createProductDto());
        verifyNoMoreInteractions(productRepository);
    }
}
