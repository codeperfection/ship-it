package com.codeperfection.shipit.exception.authorization;

public class InvalidJwtTokenException extends RuntimeException {

    public InvalidJwtTokenException(String jwtToken) {
        super("Invalid JWT token: " + jwtToken);
    }
}
