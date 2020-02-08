package com.codeperfection.shipit.service.shipping;

import com.codeperfection.shipit.dto.product.ProductDto;
import com.codeperfection.shipit.dto.shipping.CreateShippingDto;
import com.codeperfection.shipit.dto.shipping.ShippedItemDto;
import com.codeperfection.shipit.dto.shipping.ShippingDto;
import com.codeperfection.shipit.dto.transporter.TransporterDto;
import com.codeperfection.shipit.entity.*;
import com.codeperfection.shipit.exception.clienterror.ShippingImpossibleException;
import com.codeperfection.shipit.repository.ProductRepository;
import com.codeperfection.shipit.repository.ShippingRepository;
import com.codeperfection.shipit.service.shipping.placer.Item;
import com.codeperfection.shipit.service.shipping.placer.KnapsackPlacer;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
class ShippingHelperComponent {

    private ProductRepository productRepository;

    private ShippingRepository shippingRepository;

    private KnapsackPlacer knapsackPlacer;

    private ModelMapper modelMapper;

    ShippingHelperComponent(ProductRepository productRepository, ShippingRepository shippingRepository,
                            KnapsackPlacer knapsackPlacer, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.shippingRepository = shippingRepository;
        this.knapsackPlacer = knapsackPlacer;
        this.modelMapper = modelMapper;
    }

    Item[] convertToItems(List<Product> products) {
        return products.stream().flatMap(product -> IntStream.range(0, product.getCountInStock())
                .mapToObj(o -> Item.valueOf(product)))
                .toArray(Item[]::new);
    }

    Map<Product, Long> runPlacer(Transporter transporter, List<Product> products) {
        final var productToCount = knapsackPlacer.place(convertToItems(products), transporter.getCapacity())
                .getItems().stream().collect(Collectors.groupingBy(Item::getProduct,
                        () -> new TreeMap<>(Comparator.comparing(Product::getUuid)), Collectors.counting()));
        if (productToCount.isEmpty()) {
            throw new ShippingImpossibleException();
        }
        return productToCount;
    }

    void deductPlacedProductsFromStock(Map<Product, Long> placedProducts) {
        placedProducts.forEach((product, count) -> {
            product.setCountInStock(product.getCountInStock() - count.intValue());
            productRepository.save(product);
        });
    }

    Shipping saveShipping(CreateShippingDto createShippingDto, Transporter transporter, User user,
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

    List<ShippedItem> createShippedItems(Map<Product, Long> placedProducts) {
        return placedProducts.entrySet().stream().map(entry ->
                ShippedItem.builder()
                        .uuid(UUID.randomUUID())
                        .count(entry.getValue().intValue())
                        .product(entry.getKey())
                        .build())
                .collect(Collectors.toList());
    }

    ShippingDto mapToDto(Shipping shipping) {
        return ShippingDto.builder()
                .uuid(shipping.getUuid())
                .name(shipping.getName())
                .createdAt(shipping.getCreatedAt().atZoneSameInstant(shipping.getCreatedAtZoneId()).toOffsetDateTime())
                .transporter(modelMapper.map(shipping.getTransporter(), TransporterDto.class))
                .shippedItems(shipping.getShippedItems().stream()
                        .map(this::mapToDto).collect(Collectors.toList()))
                .build();
    }

    private ShippedItemDto mapToDto(ShippedItem shippedItem) {
        return new ShippedItemDto(shippedItem.getUuid(), modelMapper.map(shippedItem.getProduct(), ProductDto.class),
                shippedItem.getCount());
    }
}
