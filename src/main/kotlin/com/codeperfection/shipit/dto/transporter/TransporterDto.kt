package com.codeperfection.shipit.dto.transporter

import com.codeperfection.shipit.entity.Transporter
import java.util.*

data class TransporterDto(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val capacity: Int
) {

    companion object {

        fun fromEntity(transporter: Transporter) = TransporterDto(
            id = transporter.id,
            userId = transporter.userId,
            name = transporter.name,
            capacity = transporter.capacity
        )
    }
}
