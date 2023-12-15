package com.codeperfection.shipit.exception

data class InternalServerErrorException(val errorMessage: String) : RuntimeException(errorMessage)
