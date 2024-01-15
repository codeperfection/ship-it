package com.codeperfection.shipit.exception

import java.util.*

data class ShippingImpossibleException(val userId: UUID, val transporterId: UUID) :
    RuntimeException("Shipping is impossible with transporter with ID $transporterId for user with ID $userId")
