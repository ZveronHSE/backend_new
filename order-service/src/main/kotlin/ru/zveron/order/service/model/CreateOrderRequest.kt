package ru.zveron.order.service.model

import ru.zveron.order.persistence.model.constant.ServiceDeliveryType
import ru.zveron.order.persistence.model.constant.ServiceType
import java.time.LocalDate
import java.time.LocalTime

data class CreateOrderRequest(
    val profileId: Long,
    val animalId: Long,
    val subwayId: Int? = null,
    val description: String,
    val price: Long,
    val title: String,
    val serviceDateFrom: LocalDate,
    val serviceDateTo: LocalDate,
    val timeWindowFrom: LocalTime,
    val timeWindowTo: LocalTime,
    val serviceType: ServiceType,
    val serviceDeliveryType: ServiceDeliveryType,
)