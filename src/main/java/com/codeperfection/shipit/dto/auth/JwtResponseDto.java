package com.codeperfection.shipit.dto.auth;

import lombok.Value;

@Value
public class JwtResponseDto {

    String accessToken;

    String subject;

    String tokenType = "bearer";

    Long expiresIn;
}
