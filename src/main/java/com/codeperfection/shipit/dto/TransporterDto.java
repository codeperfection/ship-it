package com.codeperfection.shipit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransporterDto {

    private UUID uuid;

    @NotBlank
    private String name;

    @NotNull
    @Min(0)
    private Integer capacity;
}
