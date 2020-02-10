package com.codeperfection.shipit.exception.clienterror;

import com.codeperfection.shipit.exception.errordto.ErrorType;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ShippingInactiveTransporterException extends ClientErrorException {

    public ShippingInactiveTransporterException(UUID transporterUuid) {
        super(String.format("Transporter with '%s' identifier is inactive, " +
                        "thus it cannot be used in shipping creation", transporterUuid),
                HttpStatus.CONFLICT, ErrorType.SHIPPING_IMPOSSIBLE);
    }
}
