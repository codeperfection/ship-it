package com.codeperfection.shipit.service;

import com.codeperfection.shipit.dto.common.PageDto;
import com.codeperfection.shipit.dto.common.PaginationFilterDto;
import com.codeperfection.shipit.dto.product.CreateProductDto;
import com.codeperfection.shipit.dto.product.ProductDto;
import com.codeperfection.shipit.dto.product.UpdateCountInStockDto;
import com.codeperfection.shipit.dto.product.UpdateProductDto;
import com.codeperfection.shipit.entity.Product;
import com.codeperfection.shipit.entity.User;
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

import static com.codeperfection.shipit.service.ServiceUtil.applyChangeIfNeeded;

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
    public ProductDto save(CreateProductDto createProductDto, AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var product = productRepository.save(createProduct(createProductDto, user));
        return modelMapper.map(product, ProductDto.class);
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
                .elements(productsPage.stream().map(product ->
                        modelMapper.map(product, ProductDto.class)).collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public ProductDto getProduct(UUID productUuid, AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        return modelMapper.map(getProductEntity(productUuid, user), ProductDto.class);
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public ProductDto update(UUID productUuid, UpdateProductDto updateProductDto,
                             AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var product = getProductEntity(productUuid, user);

        final var newVersion = createNewVersion(product, user);
        if (applyChanges(newVersion, updateProductDto)) {
            productRepository.deactivate(productUuid);
            productRepository.save(newVersion);
            return modelMapper.map(newVersion, ProductDto.class);
        }

        return modelMapper.map(product, ProductDto.class);
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public ProductDto update(UUID productUuid, UpdateCountInStockDto countInStockDto,
                             AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        final var product = getProductEntity(productUuid, user);
        if (applyChanges(product, countInStockDto)) {
            productRepository.save(product);
        }

        return modelMapper.map(product, ProductDto.class);
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public void delete(UUID productUuid, AuthenticatedUser authenticatedUser) {
        final var user = User.withUuid(authenticatedUser.getUuid());
        if (productRepository.deleteByUuidAndUser(productUuid, user) == 0L) {
            throw new EntityNotFoundException(productUuid);
        }
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

    private Product getProductEntity(UUID productUuid, User user) {
        return productRepository.findByUuidAndUser(productUuid, user)
                .orElseThrow(() -> new EntityNotFoundException(productUuid));
    }

    private boolean applyChanges(Product existing, UpdateProductDto updateDto) {
        boolean changed = applyChangeIfNeeded(existing.getName(), updateDto.getName(), existing::setName);
        changed |= applyChangeIfNeeded(existing.getPrice(), updateDto.getPrice(), existing::setPrice);
        changed |= applyChangeIfNeeded(existing.getVolume(), updateDto.getVolume(), existing::setVolume);
        return changed;
    }

    private boolean applyChanges(Product existing, UpdateCountInStockDto updateDto) {
        return applyChangeIfNeeded(existing.getCountInStock(), updateDto.getCountInStock(), existing::setCountInStock);
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
}
