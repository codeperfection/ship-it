package com.codeperfection.shipit.exception;

import com.codeperfection.shipit.exception.clienterror.ClientErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientErrorException.class)
    public ResponseEntity<ApiError> clientErrorException(ClientErrorException e) {
        log.error(e.getMessage(), e.getCause());
        return ResponseEntity.status(e.getStatus()).contentType(MediaType.APPLICATION_JSON)
                .body(ApiError.create(e.getStatus(), e.getErrorType(), e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> httpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error(e.getMessage());
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(ApiError.badRequest(ErrorType.INVALID_PAYLOAD, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> httpMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage());
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(ApiFieldError.badRequest("Field validation failed",
                        e.getBindingResult().getFieldErrors().stream().map(error ->
                                new ApiFieldError.FieldErrorInfo(error.getField(), error.getDefaultMessage()))
                                .collect(Collectors.toList())));
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ApiError> internalServerErrorException(InternalServerErrorException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                .body(ApiError.internalServerError(e.getMessage()));
    }
}
