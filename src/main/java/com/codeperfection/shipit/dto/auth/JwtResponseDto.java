package com.codeperfection.shipit.dto.auth;

import lombok.Value;

@Value
public class JwtResponseDto {

    private String accessToken;

    private String subject;

    private String tokenType = "bearer";

    private Long expiresIn;
}
