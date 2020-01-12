package com.codeperfection.shipit.exception.clienterror;

import com.codeperfection.shipit.exception.ErrorType;
import org.springframework.http.HttpStatus;

public class UsernameAlreadyTakenException extends ClientErrorException {

    public UsernameAlreadyTakenException(String username) {
        super(String.format("Username '%s' is already taken", username), HttpStatus.CONFLICT,
                ErrorType.USERNAME_ALREADY_TAKEN);
    }
}
