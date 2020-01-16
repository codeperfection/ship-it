package com.codeperfection.shipit.util;

import com.codeperfection.shipit.dto.PageDto;
import com.codeperfection.shipit.dto.PaginationFilterDto;
import com.codeperfection.shipit.dto.TransporterDto;
import com.codeperfection.shipit.entity.Transporter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class ShippingFixtureFactory {

    private static final UUID TRANSPORTER_UUID = UUID.fromString("f4e22f5c-a668-42c3-8588-1eb4d4d0adfd");

    private static final String TRANSPORTER_NAME = "Transporter1";

    private static final int CAPACITY = 50;

    private static final OffsetDateTime CREATION_DATE = OffsetDateTime.parse("2020-01-12T18:30:46.954Z");

    public static Transporter createTransporter() {
        return new Transporter(TRANSPORTER_UUID, TRANSPORTER_NAME, CAPACITY, CREATION_DATE, true,
                AuthenticationFixtureFactory.createUser());
    }

    public static TransporterDto createTransporterDto() {
        return new TransporterDto(TRANSPORTER_UUID, TRANSPORTER_NAME, CAPACITY);
    }

    public static PaginationFilterDto createPaginationFilterDto() {
        return new PaginationFilterDto(0, 1);
    }

    public static <T> PageDto<T> createPageDto(T item) {
        return new PageDto<>(1, 1, List.of(item));
    }
}
