package com.codeperfection.shipit.exception;

import lombok.Value;

import java.time.OffsetDateTime;

@Value
public class ApiError {

    OffsetDateTime timestamp;

    Integer status;

    ErrorType error;

    String message;
}
