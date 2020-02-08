package com.codeperfection.shipit.util;

import com.codeperfection.shipit.dto.product.CreateProductDto;
import com.codeperfection.shipit.dto.product.ProductDto;
import com.codeperfection.shipit.entity.Product;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ProductFixtureFactory {

    private static final UUID PRODUCT_UUID = UUID.fromString("601ed547-ec86-4da1-9a5a-7b81eda0ff51");

    private static final String PRODUCT_NAME = "Product1";

    private static final int VOLUME = 100;

    private static final int PRICE = 12;

    private static final int COUNT_IN_STOCK = 7;

    private static final OffsetDateTime CREATION_DATE = OffsetDateTime.parse("2020-01-12T19:30:46.954Z");

    public static Product createProduct() {
        return new Product(PRODUCT_UUID, PRODUCT_NAME, VOLUME, PRICE, COUNT_IN_STOCK, CREATION_DATE, true,
                AuthenticationFixtureFactory.createUser());
    }

    public static ProductDto createProductDto() {
        return new ProductDto(PRODUCT_UUID, PRODUCT_NAME, VOLUME, PRICE, COUNT_IN_STOCK);
    }

    public static CreateProductDto createCreateProductDto() {
        return new CreateProductDto(PRODUCT_NAME, VOLUME, PRICE, COUNT_IN_STOCK);
    }
}
