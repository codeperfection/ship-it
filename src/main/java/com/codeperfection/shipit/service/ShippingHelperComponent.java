package com.codeperfection.shipit.service;

import com.codeperfection.shipit.dto.*;
import com.codeperfection.shipit.entity.*;
import com.codeperfection.shipit.placer.Item;
import com.codeperfection.shipit.repository.ProductRepository;
import com.codeperfection.shipit.repository.ShippingRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ShippingHelperComponent {

    private ProductRepository productRepository;

    private ShippingRepository shippingRepository;

    private ModelMapper modelMapper;

    public ShippingHelperComponent(ProductRepository productRepository, ShippingRepository shippingRepository,
                                   ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.shippingRepository = shippingRepository;
        this.modelMapper = modelMapper;
    }

    public Item[] convertToItems(List<Product> products) {
        int allItemsCount = products.stream().map(Product::getCountInStock).mapToInt(Integer::intValue).sum();
        final var items = new Item[allItemsCount];
        int itemsIndex = 0;
        for (final var product : products) {
            for (int i = 0; i < product.getCountInStock(); i++) {
                items[itemsIndex++] = new Item(product, product.getVolume(), product.getPrice());
            }
        }
        return items;
    }

    public void deductPlacedProductsFromStock(Map<Product, Long> placedProducts) {
        placedProducts.forEach((product, count) -> {
            product.setCountInStock(product.getCountInStock() - count.intValue());
            productRepository.save(product);
        });
    }

    public Shipping saveShipping(CreateShippingDto createShippingDto, Transporter transporter, User user,
                                 List<ShippedItem> shippedItems) {
        final var shipping = Shipping.builder()
                .uuid(UUID.randomUUID())
                .name(createShippingDto.getName())
                .user(user)
                .createdAt(OffsetDateTime.now())
                .createdAtZoneId(createShippingDto.getTimeZoneName())
                .transporter(transporter)
                .build();
        shippedItems.forEach(shippedItem -> shippedItem.setShipping(shipping));
        shipping.setShippedItems(shippedItems);
        return shippingRepository.save(shipping);
    }

    public List<ShippedItem> createShippedItems(Map<Product, Long> placedProducts) {
        return placedProducts.entrySet().stream().map(entry ->
                ShippedItem.builder()
                        .uuid(UUID.randomUUID())
                        .count(entry.getValue().intValue())
                        .product(entry.getKey())
                        .build())
                .collect(Collectors.toList());
    }

    public ShippingDto mapToDto(Shipping shipping) {
        final var shippedItemDtos = shipping.getShippedItems().stream().map(shippedItem ->
                new ShippedItemDto(shippedItem.getUuid(), modelMapper.map(shippedItem.getProduct(), ProductDto.class),
                        shippedItem.getCount()))
                .collect(Collectors.toList());
        return ShippingDto.builder()
                .uuid(shipping.getUuid())
                .name(shipping.getName())
                .createdAt(shipping.getCreatedAt().atZoneSameInstant(shipping.getCreatedAtZoneId()).toOffsetDateTime())
                .transporter(modelMapper.map(shipping.getTransporter(), TransporterDto.class))
                .shippedItems(shippedItemDtos)
                .build();
    }
}
