package com.codeperfection.shipit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpDto {

    @NotNull
    @Pattern(regexp = "^[\\p{Alnum}]{4,64}$")
    private String username;

    @NotNull
    @Size(min = 4, max = 128)
    private String password;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String name;
}
