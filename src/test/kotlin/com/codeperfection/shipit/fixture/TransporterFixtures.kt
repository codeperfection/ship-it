package com.codeperfection.shipit.fixture

import com.codeperfection.shipit.dto.transporter.CreateTransporterDto
import com.codeperfection.shipit.dto.transporter.TransporterDto
import com.codeperfection.shipit.entity.Transporter
import java.time.OffsetDateTime
import java.util.*

val TRANSPORTER_ID: UUID = UUID.fromString("802d1ee3-f7a7-4071-8620-dbeddb8b1bc5")
const val TRANSPORTER_NAME = "Transporter1"
const val TRANSPORTER_CAPACITY = 300
val TRANSPORTER_CREATION_DATE: OffsetDateTime = OffsetDateTime.parse("2023-01-13T19:30:46.954Z")

fun createTransporterFixture() = Transporter(
    id = TRANSPORTER_ID,
    userId = USER_ID,
    name = TRANSPORTER_NAME,
    capacity = TRANSPORTER_CAPACITY,
    createdAt = TRANSPORTER_CREATION_DATE,
    updatedAt = TRANSPORTER_CREATION_DATE,
    isActive = true
)

val createTransporterDtoFixture = CreateTransporterDto(
    name = TRANSPORTER_NAME,
    capacity = TRANSPORTER_CAPACITY
)

val transporterDtoFixture = TransporterDto(
    id = TRANSPORTER_ID,
    userId = USER_ID,
    name = TRANSPORTER_NAME,
    capacity = TRANSPORTER_CAPACITY
)
