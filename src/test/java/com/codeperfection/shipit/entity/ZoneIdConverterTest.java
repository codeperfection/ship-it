package com.codeperfection.shipit.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ZoneIdConverterTest {

    @InjectMocks
    private ZoneIdConverter zoneIdConverter;

    @Test
    public void convertToDatabaseColumn_IfNullGiven_ReturnsNull() {
        assertThat(zoneIdConverter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    public void convertToDatabaseColumn_IfValidValueProvided_ReturnsStringRepresentation() {
        String timeZoneName = "Europe/Berlin";
        assertThat(zoneIdConverter.convertToDatabaseColumn(ZoneId.of(timeZoneName))).isEqualTo(timeZoneName);
    }

    @Test
    public void convertToEntityAttribute_IfNullProvided_ReturnsNull() {
        assertThat(zoneIdConverter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    public void convertToEntityAttribute_IfValidStringProvided_ReturnsZoneIdRepresentation() {
        String timeZoneName = "Europe/Berlin";
        assertThat(zoneIdConverter.convertToEntityAttribute(timeZoneName)).isEqualTo(ZoneId.of(timeZoneName));
    }
}
