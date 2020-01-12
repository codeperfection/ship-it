package com.codeperfection.shipit.exception.clienterror;

import com.codeperfection.shipit.exception.ErrorType;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ClientErrorException extends RuntimeException {

    private int status;

    private ErrorType error;

    public ClientErrorException(String message, HttpStatus httpStatus, ErrorType error) {
        super(message);
        this.status = httpStatus.value();
        this.error = error;
    }
}
