package com.codeperfection.shipit.dto.transporter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransporterDto {

    @NotBlank
    @Size(max = 256)
    private String name;

    @NotNull
    @Min(0)
    private Integer capacity;
}
