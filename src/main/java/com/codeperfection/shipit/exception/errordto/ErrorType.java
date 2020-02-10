package com.codeperfection.shipit.exception.errordto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum ErrorType {
    EMAIL_ALREADY_TAKEN("email_already_taken"),
    USERNAME_ALREADY_TAKEN("username_already_taken"),
    INACTIVE_ENTITY_CHANGE("inactive_entity_change"),
    INCORRECT_PASSWORD("incorrect_password"),
    SHIPPING_IMPOSSIBLE("shipping_impossible"),
    UNAUTHORIZED("unauthorized"),
    INVALID_REQUEST("invalid_request"),
    INVALID_PATH_VARIABLE("invalid_path_variable"),
    INTERNAL_SERVER_ERROR("internal_server_error"),
    NOT_FOUND("not_found");

    @JsonValue
    @Getter
    private final String displayName;

    ErrorType(String displayName) {
        this.displayName = displayName;
    }
}
