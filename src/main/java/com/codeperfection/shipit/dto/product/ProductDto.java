package com.codeperfection.shipit.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private UUID uuid;

    private String name;

    private Integer volume;

    private Integer price;

    private Integer countInStock;
}
