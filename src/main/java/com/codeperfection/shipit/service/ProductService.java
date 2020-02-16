package com.codeperfection.shipit.service;

import com.codeperfection.shipit.dto.common.PageDto;
import com.codeperfection.shipit.dto.common.PaginationFilterDto;
import com.codeperfection.shipit.dto.product.CreateProductDto;
import com.codeperfection.shipit.dto.product.ProductDto;
import com.codeperfection.shipit.dto.product.UpdateCountInStockDto;
import com.codeperfection.shipit.dto.product.UpdateProductDto;
import com.codeperfection.shipit.entity.Product;
import com.codeperfection.shipit.entity.User;
import com.codeperfection.shipit.exception.clienterror.CannotChangeInactiveEntityException;
import com.codeperfection.shipit.exception.clienterror.EntityNotFoundException;
import com.codeperfection.shipit.repository.ProductRepository;
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
public class ProductService {

    private ProductRepository productRepository;

    private ModelMapper modelMapper;

    public ProductService(ProductRepository productRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public ProductDto createProduct(CreateProductDto createProductDto, AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var product = productRepository.save(createProduct(createProductDto, user));
        return mapToDto(product);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public PageDto<ProductDto> getProducts(PaginationFilterDto paginationFilterDto,
                                           AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var productsPage = productRepository.findByUserAndIsActiveTrue(user,
                PageRequest.of(paginationFilterDto.getPage(), paginationFilterDto.getSize(), Sort.by("createdAt")));

        return PageDto.<ProductDto>builder()
                .totalPages(productsPage.getTotalPages())
                .totalElements(productsPage.getTotalElements())
                .elements(productsPage.stream().map(this::mapToDto).collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public ProductDto getProduct(UUID productUuid, AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        return mapToDto(getProduct(productUuid, user));
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public ProductDto updateProduct(UUID productUuid, UpdateProductDto updateProductDto,
                                    AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        final Product currentProduct = getActiveProductForUpdate(productUuid, user);

        final var newProduct = createNewVersion(currentProduct, user);
        if (applyChanges(newProduct, updateProductDto)) {
            removeStockAndDeactivate(currentProduct);
            productRepository.save(newProduct);
            return mapToDto(newProduct);
        }

        return mapToDto(currentProduct);
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public ProductDto updateCountInStock(UUID productUuid, UpdateCountInStockDto countInStockDto,
                                         AuthenticatedUser authenticatedUser) {
        final var product = getActiveProductForUpdate(productUuid, User.withUuid(authenticatedUser.getUuid()));
        if (applyChanges(product, countInStockDto)) {
            productRepository.save(product);
        }

        return mapToDto(product);
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public void deleteProduct(UUID productUuid, AuthenticatedUser authenticatedUser) {
        deactivate(getActiveProductForUpdate(productUuid, User.withUuid(authenticatedUser.getUuid())));
    }

    private Product createProduct(CreateProductDto createProductDto, User user) {
        return Product.builder()
                .uuid(UUID.randomUUID())
                .name(createProductDto.getName())
                .volume(createProductDto.getVolume())
                .price(createProductDto.getPrice())
                .countInStock(createProductDto.getCountInStock())
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .user(user)
                .build();
    }

    private ProductDto mapToDto(Product product) {
        return modelMapper.map(product, ProductDto.class);
    }

    private Product getProduct(UUID productUuid, User user) {
        return productRepository.findByUuidAndUser(productUuid, user)
                .orElseThrow(() -> new EntityNotFoundException(productUuid));
    }

    private Product getActiveProductForUpdate(UUID productUuid, User user) {
        final var product = getProduct(productUuid, user);
        if (!product.getIsActive()) {
            throw new CannotChangeInactiveEntityException(productUuid);
        }
        return product;
    }

    private Product createNewVersion(Product product, User user) {
        return Product.builder()
                .uuid(UUID.randomUUID())
                .name(product.getName())
                .volume(product.getVolume())
                .price(product.getPrice())
                .countInStock(product.getCountInStock())
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .user(user)
                .build();
    }

    private boolean applyChanges(Product product, UpdateProductDto updateDto) {
        boolean changed = CommonServiceUtil.applyChangeIfNeeded(
                product.getName(), updateDto.getName(), product::setName);
        changed |= CommonServiceUtil.applyChangeIfNeeded(
                product.getPrice(), updateDto.getPrice(), product::setPrice);
        changed |= CommonServiceUtil.applyChangeIfNeeded(
                product.getVolume(), updateDto.getVolume(), product::setVolume);
        return changed;
    }

    private void removeStockAndDeactivate(Product product) {
        product.setCountInStock(0);
        deactivate(product);
    }

    private void deactivate(Product product) {
        product.setIsActive(false);
        productRepository.save(product);
    }

    private boolean applyChanges(Product product, UpdateCountInStockDto updateDto) {
        return CommonServiceUtil.applyChangeIfNeeded(
                product.getCountInStock(), updateDto.getCountInStock(), product::setCountInStock);
    }
}
