package com.codeperfection.shipit.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class StockItem {

    @Id
    private UUID uuid;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_uuid")
    private Product product;

    @NotNull
    private Integer count;
}
