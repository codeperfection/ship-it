package com.codeperfection.shipit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    private UUID uuid;

    @NotNull
    private String name;

    @NotNull
    private Integer volume;

    @NotNull
    private Integer price;

    @NotNull
    private Integer countInStock;

    @NotNull
    private OffsetDateTime createdAt;

    @NotNull
    private Boolean isActive;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_uuid")
    private User user;
}
