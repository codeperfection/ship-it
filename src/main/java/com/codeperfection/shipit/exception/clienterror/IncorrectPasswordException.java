package com.codeperfection.shipit.exception.clienterror;

import com.codeperfection.shipit.exception.ErrorType;
import org.springframework.http.HttpStatus;

public class IncorrectPasswordException extends ClientErrorException {

    public IncorrectPasswordException() {
        super("Incorrect password", HttpStatus.BAD_REQUEST, ErrorType.INCORRECT_PASSWORD);
    }
}
