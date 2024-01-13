package com.codeperfection.shipit.exception.clienterror

import com.codeperfection.shipit.exception.dto.ErrorType
import org.springframework.http.HttpStatus
import java.util.*

data class NotFoundException(val id: UUID, val userId: UUID) : ClientErrorException(
    status = HttpStatus.NOT_FOUND,
    errorType = ErrorType.NOT_FOUND,
    message = "Entity with ID '$id' not found for user with ID '$userId'"
)
