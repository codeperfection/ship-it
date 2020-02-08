package com.codeperfection.shipit.dto.auth;

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
    @Size(max = 256)
    private String username;

    @NotNull
    @Size(min = 4, max = 128)
    private String password;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(max = 256)
    private String name;
}
