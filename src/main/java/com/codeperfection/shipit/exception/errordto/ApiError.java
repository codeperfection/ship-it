package com.codeperfection.shipit.exception.errordto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@AllArgsConstructor
public class ApiError {

    private final OffsetDateTime timestamp;

    private final Integer status;

    private final ErrorType errorType;

    private final String message;

    public static ApiError badRequest(ErrorType errorType, String message) {
        return new ApiError(OffsetDateTime.now(ZoneOffset.UTC), HttpStatus.BAD_REQUEST.value(), errorType, message);
    }

    public static ApiError unauthorized(String message) {
        return new ApiError(OffsetDateTime.now(ZoneOffset.UTC), HttpStatus.UNAUTHORIZED.value(),
                ErrorType.UNAUTHORIZED, message);
    }

    public static ApiError internalServerError(String message) {
        return new ApiError(OffsetDateTime.now(ZoneOffset.UTC), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorType.INTERNAL_SERVER_ERROR, message);
    }

    public static ApiError create(int httpStatus, ErrorType errorType, String message) {
        return new ApiError(OffsetDateTime.now(ZoneOffset.UTC), httpStatus, errorType, message);
    }
}
