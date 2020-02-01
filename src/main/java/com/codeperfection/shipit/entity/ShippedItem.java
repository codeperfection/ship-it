package com.codeperfection.shipit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippedItem {

    @Id
    private UUID uuid;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_uuid")
    private Product product;

    @NotNull
    private Integer count;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "shipping_uuid")
    private Shipping shipping;
}
