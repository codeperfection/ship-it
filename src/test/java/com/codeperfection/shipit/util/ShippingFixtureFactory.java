package com.codeperfection.shipit.util;

import com.codeperfection.shipit.dto.shipping.CreateShippingDto;
import com.codeperfection.shipit.dto.shipping.ShippedItemDto;
import com.codeperfection.shipit.dto.shipping.ShippingDto;
import com.codeperfection.shipit.entity.ShippedItem;
import com.codeperfection.shipit.entity.Shipping;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static com.codeperfection.shipit.util.TransporterFixtureFactory.createTransporter;
import static com.codeperfection.shipit.util.TransporterFixtureFactory.createTransporterDto;

public final class ShippingFixtureFactory {

    // prevent instantiation of class
    private ShippingFixtureFactory() {
    }

    private static final UUID SHIPPING_UUID = UUID.fromString("7944483a-c23d-4524-b4b9-29b5568a12ac");

    private static final String SHIPPING_NAME = "Shipping1";

    private static final OffsetDateTime CREATION_DATE = OffsetDateTime.parse("2020-01-12T18:30:46.954Z");

    public static CreateShippingDto createCreateShippingDto() {
        return new CreateShippingDto(SHIPPING_NAME, createTransporter().getUuid(), ZoneId.of("UTC"));
    }

    public static List<ShippedItem> createShippedItems() {
        return List.of(new ShippedItem(UUID.fromString("57298b5a-235d-4341-851e-af86b899e15a"),
                ProductFixtureFactory.createProduct(), 2, null));
    }

    public static Shipping createShipping() {
        final var shipping = Shipping.builder()
                .uuid(SHIPPING_UUID)
                .name(SHIPPING_NAME)
                .createdAt(CREATION_DATE)
                .createdAtZoneId(ZoneId.of("UTC"))
                .transporter(createTransporter())
                .user(AuthenticationFixtureFactory.createUser())
                .build();
        final var shippedItems = createShippedItems();
        shippedItems.forEach(shippedItem -> shippedItem.setShipping(shipping));
        shipping.setShippedItems(shippedItems);
        return shipping;
    }

    public static ShippingDto createShippingDto() {
        final var shippedItemDto = new ShippedItemDto(UUID.fromString("57298b5a-235d-4341-851e-af86b899e15a"),
                ProductFixtureFactory.createProductDto(), 2);
        return ShippingDto.builder()
                .uuid(SHIPPING_UUID)
                .name(SHIPPING_NAME)
                .createdAt(CREATION_DATE)
                .transporter(createTransporterDto())
                .shippedItems(List.of(shippedItemDto))
                .build();
    }
}
