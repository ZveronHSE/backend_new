package ru.zveron.order.persistence.repository.model

import ru.zveron.order.persistence.model.constant.ServiceDeliveryType
import ru.zveron.order.persistence.model.constant.ServiceType
import java.time.Instant
import java.time.LocalDate

data class OrderLotWrapper(
    val id: Long,
    val animalId: Long,
    val price: Long,
    val createdAt: Instant,
    val title: String,
    val subwayId: Int?,
    val serviceDateFrom: LocalDate,
    val serviceDateTo: LocalDate?,
    val serviceType: ServiceType,
    val serviceDeliveryType: ServiceDeliveryType,
)