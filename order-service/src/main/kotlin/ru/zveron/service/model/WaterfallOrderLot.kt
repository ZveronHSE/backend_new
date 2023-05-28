package ru.zveron.service.model

import java.time.Instant
import java.time.LocalDate

data class WaterfallOrderLot(
    val id: Long,
    val animal: Animal,
    val price: Long,
    val title: String,
    val subway: SubwayStation?,
    val createdAt: Instant,
    val serviceDateFrom: LocalDate,
    val serviceDateTo: LocalDate?,
)
