package com.codeperfection.shipit.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDto {

    @NotBlank
    private String oldPassword;

    @NotNull
    @Size(min = 4, max = 128)
    private String newPassword;
}
