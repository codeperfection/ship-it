package com.codeperfection.shipit.exception;

import com.codeperfection.shipit.exception.clienterror.ClientErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(ClientErrorException.class)
    public ResponseEntity<ApiError> clientErrorException(ClientErrorException e) {
        logger.error(e.getMessage(), e.getCause());
        return ResponseEntity.status(e.getStatus()).contentType(MediaType.APPLICATION_JSON)
                .body(new ApiError(OffsetDateTime.now(ZoneOffset.UTC), e.getStatus(), e.getError(), e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> httpMessageNotReadableException(HttpMessageNotReadableException e) {
        logger.error(e.getMessage());
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(new ApiError(OffsetDateTime.now(ZoneOffset.UTC), HttpStatus.BAD_REQUEST.value(),
                        ErrorType.INVALID_PAYLOAD, e.getMessage()));
    }

    @SuppressWarnings("ConstantConditions")
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> httpMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.error(e.getMessage());
        FieldError fieldError = e.getBindingResult().getFieldError();
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(new ApiError(OffsetDateTime.now(ZoneOffset.UTC), HttpStatus.BAD_REQUEST.value(),
                        ErrorType.INVALID_PAYLOAD, String.format("Invalid field '%s': %s", fieldError.getField(),
                        fieldError.getDefaultMessage())));
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ApiError> internalServerErrorException(InternalServerErrorException e) {
        logger.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                .body(new ApiError(OffsetDateTime.now(ZoneOffset.UTC), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ErrorType.INTERNAL_SERVER_ERROR, e.getMessage()));
    }
}
