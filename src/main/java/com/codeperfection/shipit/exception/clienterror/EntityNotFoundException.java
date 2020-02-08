package com.codeperfection.shipit.exception.clienterror;

import com.codeperfection.shipit.exception.errordto.ErrorType;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class EntityNotFoundException extends ClientErrorException {

    public EntityNotFoundException(UUID uuid) {
        super(String.format("Entity with UUID '%s' not found", uuid), HttpStatus.NOT_FOUND, ErrorType.NOT_FOUND);
    }
}
