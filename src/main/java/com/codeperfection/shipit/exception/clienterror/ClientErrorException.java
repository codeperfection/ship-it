package com.codeperfection.shipit.exception.clienterror;

import com.codeperfection.shipit.exception.ErrorType;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ClientErrorException extends RuntimeException {

    private int status;

    private ErrorType errorType;

    public ClientErrorException(String message, HttpStatus httpStatus, ErrorType errorType) {
        super(message);
        this.status = httpStatus.value();
        this.errorType = errorType;
    }
}
