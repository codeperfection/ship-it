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
    public void convertToDatabaseColumnIfNullGivenReturnsNull() {
        assertThat(zoneIdConverter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    public void convertToDatabaseColumnIfValidValueProvidedReturnsStringRepresentation() {
        String timeZoneName = "Europe/Berlin";
        assertThat(zoneIdConverter.convertToDatabaseColumn(ZoneId.of(timeZoneName))).isEqualTo(timeZoneName);
    }

    @Test
    public void convertToEntityAttributeIfNullProvidedReturnsNull() {
        assertThat(zoneIdConverter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    public void convertToEntityAttributeIfValidStringProvidedReturnsZoneIdRepresentation() {
        String timeZoneName = "Europe/Berlin";
        assertThat(zoneIdConverter.convertToEntityAttribute(timeZoneName)).isEqualTo(ZoneId.of(timeZoneName));
    }
}
