package ru.zveron.order.service.dto

import ru.zveron.order.persistence.model.constant.Status
import ru.zveron.order.service.constant.ServiceDeliveryType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class GetOrderResponse(
        val id: Long,

        val profile: Profile,
        val animal: Animal,
        val subwayStation: SubwayStation,

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

