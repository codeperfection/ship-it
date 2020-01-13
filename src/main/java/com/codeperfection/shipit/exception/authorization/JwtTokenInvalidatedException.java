package com.codeperfection.shipit.exception.authorization;

public class JwtTokenInvalidatedException extends RuntimeException {

    public JwtTokenInvalidatedException(String jwtToken) {
        super("JWT token was invalidated: " + jwtToken);
    }
}
