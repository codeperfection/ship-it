package com.codeperfection.shipit.dto.transporter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransporterDto {

    // Not blank and not empty, but can be null.
    @Pattern(regexp = "(.|\\s)*\\S(.|\\s)*")
    private String name;

    @Min(0)
    private Integer capacity;
}
