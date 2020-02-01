package com.codeperfection.shipit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.ZoneId;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateShippingDto {

    @NotEmpty
    private String name;

    @NotNull
    private UUID transporterUuid;

    @NotNull
    private ZoneId timeZoneName;
}
