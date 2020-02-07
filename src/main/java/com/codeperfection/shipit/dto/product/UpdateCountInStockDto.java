package com.codeperfection.shipit.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCountInStockDto {

    @NotNull
    @Min(0)
    private Integer countInStock;
}
