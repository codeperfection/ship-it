package com.codeperfection.shipit.service;

import com.codeperfection.shipit.dto.PageDto;
import com.codeperfection.shipit.dto.PaginationFilterDto;
import com.codeperfection.shipit.dto.ProductDto;
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
    public ProductDto save(ProductDto productDto, AuthenticatedUser authenticatedUser) {
        final var product = productRepository.save(createProduct(productDto, authenticatedUser.getUuid()));
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
                .orElseThrow(() -> new EntityNotFoundException(authenticatedUser.getUuid()));
        return modelMapper.map(product, ProductDto.class);
    }

    private Product createProduct(ProductDto productDto, UUID userUuid) {
        return Product.builder()
                .uuid(UUID.randomUUID())
                .name(productDto.getName())
                .volume(productDto.getVolume())
                .price(productDto.getPrice())
                .countInStock(productDto.getCountInStock())
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .user(User.withUuid(userUuid))
                .build();
    }
}
