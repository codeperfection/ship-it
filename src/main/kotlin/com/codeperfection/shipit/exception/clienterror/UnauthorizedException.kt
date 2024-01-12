package com.codeperfection.shipit.exception.clienterror

import com.codeperfection.shipit.exception.dto.ErrorType
import org.springframework.http.HttpStatus

object UnauthorizedException : ClientErrorException(
    status = HttpStatus.FORBIDDEN,
    errorType = ErrorType.AUTHORIZATION_ERROR,
    message = "Authorization error"
)
