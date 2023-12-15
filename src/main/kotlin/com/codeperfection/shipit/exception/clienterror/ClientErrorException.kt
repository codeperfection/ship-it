package com.codeperfection.shipit.exception.clienterror

import com.codeperfection.shipit.exception.dto.ErrorType
import org.springframework.http.HttpStatus

abstract class ClientErrorException(
    val status: HttpStatus,
    val errorType: ErrorType,
    message: String
) : RuntimeException(message)
