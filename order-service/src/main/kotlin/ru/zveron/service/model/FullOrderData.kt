package ru.zveron.service.model

import ru.zveron.persistence.model.constant.Status
import ru.zveron.service.constant.ServiceDeliveryType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class FullOrderData(
        val id: Long,

        val profile: Profile,
        val animal: Animal,
        val subwayStation: SubwayStation? = null,

        val title: String,
        val price: String,
        // date of order delivery formatted as dd.MM.yyyy - dd.MM.yyyy
        val serviceDateFrom: LocalDate,
        val serviceDateTo: LocalDate? = null,
        // time of order delivery formatted as HH:mm (- HH:mm)
        val timeWindowFrom: LocalTime? = null,
        val timeWindowTo: LocalTime? = null,

        val description: String,
        // delivery type, in person or remote
        val serviceDeliveryType: ServiceDeliveryType,
        val orderStatus: Status,
        val createdAt: Instant,
)

