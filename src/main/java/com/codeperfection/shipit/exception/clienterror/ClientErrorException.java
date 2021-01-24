package com.codeperfection.shipit.exception.clienterror;

import com.codeperfection.shipit.exception.errordto.ErrorType;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ClientErrorException extends RuntimeException {

    private final int status;

    private final ErrorType errorType;

    public ClientErrorException(String message, HttpStatus httpStatus, ErrorType errorType) {
        super(message);
        this.status = httpStatus.value();
        this.errorType = errorType;
    }
}
