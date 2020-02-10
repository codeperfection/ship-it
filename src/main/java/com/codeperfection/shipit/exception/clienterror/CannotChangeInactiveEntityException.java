package com.codeperfection.shipit.exception.clienterror;

import com.codeperfection.shipit.exception.errordto.ErrorType;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class CannotChangeInactiveEntityException extends ClientErrorException {

    public CannotChangeInactiveEntityException(UUID entityUuid) {
        super(String.format("Entity with '%s' identifier is inactive, thus it cannot be changed", entityUuid),
                HttpStatus.CONFLICT, ErrorType.INACTIVE_ENTITY_CHANGE);
    }
}
