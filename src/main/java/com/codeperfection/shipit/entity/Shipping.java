package com.codeperfection.shipit.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString(exclude = "shippedItems")
@EqualsAndHashCode(exclude = "shippedItems")
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transporter_uuid")
    private Transporter transporter;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_uuid")
    private User user;

    @NotNull
    @OneToMany(mappedBy = "shipping", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ShippedItem> shippedItems;
}
