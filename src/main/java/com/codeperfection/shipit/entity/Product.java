package com.codeperfection.shipit.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class Product {

    @Id
    private UUID uuid;

    @NotNull
    private String name;

    @NotNull
    private Integer volume;

    @NotNull
    private OffsetDateTime createdAt;

    @NotNull
    private Boolean isActive;
}
