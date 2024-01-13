package com.codeperfection.shipit.exception

import com.codeperfection.shipit.entity.Transporter

class ShippingImpossibleException(transporter: Transporter) : RuntimeException() {

    override val message =
        "Shipping is impossible with transporter with ID ${transporter.id} for user with ID ${transporter.userId}"
}
