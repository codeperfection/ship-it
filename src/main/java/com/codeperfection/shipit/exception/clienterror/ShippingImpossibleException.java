package com.codeperfection.shipit.exception.clienterror;

import com.codeperfection.shipit.exception.ErrorType;
import org.springframework.http.HttpStatus;

public class ShippingImpossibleException extends ClientErrorException {

    public ShippingImpossibleException() {
        super("Impossible to create a shipping with given items in stock and transporter",
                HttpStatus.BAD_REQUEST, ErrorType.SHIPPING_IMPOSSIBLE);
    }
}
