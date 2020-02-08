package com.codeperfection.shipit.dto.transporter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransporterDto {

    // Not blank and not empty, but can be null.
    @Pattern(regexp = "(.|\\s)*\\S(.|\\s)*")
    @Size(max = 256)
    private String name;

    @Min(0)
    private Integer capacity;
}
