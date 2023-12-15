package com.codeperfection.shipit.dto.transporter

import java.util.*

data class TransporterDto(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val capacity: Int
)
