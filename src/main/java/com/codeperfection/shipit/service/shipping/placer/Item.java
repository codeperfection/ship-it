package com.codeperfection.shipit.service.shipping.placer;

import com.codeperfection.shipit.entity.Product;
import lombok.Value;

@Value
public class Item {

    Product product;

    int volume;

    int price;

    public static Item valueOf(Product product) {
        return new Item(product, product.getVolume(), product.getPrice());
    }
}
