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
        final var product = productRepository.save(createProduct(createProductDto, authenticatedUser.getUuid()));
        return modelMapper.map(product, ProductDto.class);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public PageDto<ProductDto> getProducts(PaginationFilterDto paginationFilterDto,
                                           AuthenticatedUser authenticatedUser) {
        final var productsPage = productRepository.findByUserAndIsActiveTrue(
                User.withUuid(authenticatedUser.getUuid()), PageRequest.of(paginationFilterDto.getPage(),
                        paginationFilterDto.getSize(), Sort.by("createdAt")));

        return PageDto.<ProductDto>builder()
                .totalPages(productsPage.getTotalPages())
                .totalElements(productsPage.getTotalElements())
                .elements(productsPage.stream().map(product ->
                        modelMapper.map(product, ProductDto.class)).collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public ProductDto getProduct(UUID uuid, AuthenticatedUser authenticatedUser) {
        final var product = productRepository.findByUuidAndUser(uuid, User.withUuid(authenticatedUser.getUuid()))
                .orElseThrow(() -> new EntityNotFoundException(uuid));
        return modelMapper.map(product, ProductDto.class);
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public ProductDto update(UUID productUuid, UpdateProductDto updateProductDto,
                             AuthenticatedUser authenticatedUser) {
        // TODO: fill body
        return null;
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public ProductDto update(UUID productUuid, UpdateCountInStockDto countInStockDto,
                             AuthenticatedUser authenticatedUser) {
        // TODO: fill body
        return null;
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public void delete(UUID productUuid, AuthenticatedUser authenticatedUser) {
        // TODO: fill body
    }

    private Product createProduct(CreateProductDto createProductDto, UUID userUuid) {
        return Product.builder()
                .uuid(UUID.randomUUID())
                .name(createProductDto.getName())
                .volume(createProductDto.getVolume())
                .price(createProductDto.getPrice())
                .countInStock(createProductDto.getCountInStock())
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .user(User.withUuid(userUuid))
                .build();
    }
}
