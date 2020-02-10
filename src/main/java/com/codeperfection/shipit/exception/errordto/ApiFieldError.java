package com.codeperfection.shipit.exception.errordto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ApiFieldError extends ApiError {

    private List<FieldErrorInfo> fieldErrors;

    public ApiFieldError(OffsetDateTime timestamp, Integer status, ErrorType errorType, String message,
                         List<FieldErrorInfo> fieldErrors) {
        super(timestamp, status, errorType, message);
        this.fieldErrors = fieldErrors;
    }

    @Value
    public static class FieldErrorInfo {
        private String fieldName;
        public String errorMessage;
    }

    public static ApiFieldError badRequest(String message, List<FieldErrorInfo> fieldErrorInfos) {
        return new ApiFieldError(OffsetDateTime.now(ZoneOffset.UTC), HttpStatus.BAD_REQUEST.value(),
                ErrorType.INVALID_REQUEST, message, fieldErrorInfos);
    }
}
