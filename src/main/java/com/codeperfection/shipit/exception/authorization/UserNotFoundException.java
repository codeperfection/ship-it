package com.codeperfection.shipit.exception.authorization;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userUuid) {
        super(String.format("User with UUID '%s' not found", userUuid));
    }
}
