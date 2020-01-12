package com.codeperfection.shipit.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class Shipping {

    @Id
    private UUID uuid;

    @NotNull
    private String name;

    @NotNull
    private OffsetDateTime createdAt;

    @Column(name = "time_zone_name")
    @Convert(converter = ZoneIdConverter.class)
    @NotNull
    private ZoneId createdAtZoneId;
}
