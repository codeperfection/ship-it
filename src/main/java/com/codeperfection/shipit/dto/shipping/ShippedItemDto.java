package com.codeperfection.shipit.dto.shipping;

import com.codeperfection.shipit.dto.product.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ShippedItemDto {

    private UUID uuid;

    private ProductDto product;

    private Integer count;
}
