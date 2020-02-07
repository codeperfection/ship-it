package com.codeperfection.shipit.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductDto {

    @NotBlank
    private String name;

    @NotNull
    @Min(1)
    private Integer volume;

    @NotNull
    @Min(1)
    private Integer price;

    @NotNull
    @Min(0)
    private Integer countInStock;
}
