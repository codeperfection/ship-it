package com.codeperfection.shipit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignInDto {

    @NotBlank
    private String usernameOrEmail;

    @NotBlank
    private String password;
}
