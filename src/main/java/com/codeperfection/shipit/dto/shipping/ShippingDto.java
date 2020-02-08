package com.codeperfection.shipit.dto.shipping;

import com.codeperfection.shipit.dto.transporter.TransporterDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class ShippingDto {

    private UUID uuid;

    private String name;

    private OffsetDateTime createdAt;

    private TransporterDto transporter;

    private List<ShippedItemDto> shippedItems;
}
