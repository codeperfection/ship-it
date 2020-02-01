package com.codeperfection.shipit.placer;

import com.codeperfection.shipit.entity.Product;
import lombok.Value;

@Value
public class Item {

    private Product product;

    private Integer volume;

    private Integer price;
}
