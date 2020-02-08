package com.codeperfection.shipit.exception.clienterror;

import com.codeperfection.shipit.exception.errordto.ErrorType;
import org.springframework.http.HttpStatus;

public class EmailAlreadyTakenException extends ClientErrorException {

    public EmailAlreadyTakenException(String email) {
        super(String.format("Email '%s' is already taken", email), HttpStatus.CONFLICT, ErrorType.EMAIL_ALREADY_TAKEN);
    }
}
