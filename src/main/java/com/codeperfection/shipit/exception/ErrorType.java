package com.codeperfection.shipit.exception;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum ErrorType {
    EMAIL_ALREADY_TAKEN("email_already_taken"),
    USERNAME_ALREADY_TAKEN("username_already_taken"),
    INCORRECT_PASSWORD("incorrect_password"),
    SHIPPING_IMPOSSIBLE("shipping_impossible"),
    UNAUTHORIZED("unauthorized"),
    INVALID_PAYLOAD("invalid_payload"),
    INTERNAL_SERVER_ERROR("internal_server_error"),
    NOT_FOUND("not_found");

    @JsonValue
    @Getter
    private final String displayName;

    ErrorType(String displayName) {
        this.displayName = displayName;
    }
}
