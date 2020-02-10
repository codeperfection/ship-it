package com.codeperfection.shipit.exception;

import com.codeperfection.shipit.exception.clienterror.ClientErrorException;
import com.codeperfection.shipit.exception.errordto.ApiError;
import com.codeperfection.shipit.exception.errordto.ApiFieldError;
import com.codeperfection.shipit.exception.errordto.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

import static com.codeperfection.shipit.exception.errordto.ErrorType.INVALID_PATH_VARIABLE;

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
                .body(ApiError.badRequest(ErrorType.INVALID_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        if (e.getParameter().getParameterAnnotation(PathVariable.class) == null) {
            throw e;
        }
        log.error(e.getMessage());
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(ApiError.badRequest(
                INVALID_PATH_VARIABLE, String.format("Validation of path variable '%s' failed. Error: %s",
                        e.getName(), e.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage());
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(ApiFieldError.badRequest("Field validation failed",
                        e.getBindingResult().getFieldErrors().stream().map(error ->
                                new ApiFieldError.FieldErrorInfo(error.getField(), error.getDefaultMessage()))
                                .collect(Collectors.toList())));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> bindException(BindException e) {
        log.error(e.getMessage());
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(
                ApiFieldError.badRequest("Field validation failed", e.getBindingResult().getFieldErrors()
                        .stream().map(error -> new ApiFieldError.FieldErrorInfo(error.getField(),
                                error.getDefaultMessage())).collect(Collectors.toList())));
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ApiError> internalServerErrorException(InternalServerErrorException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                .body(ApiError.internalServerError(e.getMessage()));
    }
}
