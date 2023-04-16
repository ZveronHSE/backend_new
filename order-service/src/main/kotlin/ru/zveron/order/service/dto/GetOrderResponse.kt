package ru.zveron.order.service.dto

import ru.zveron.order.service.constant.ServiceDeliveryType

data class GetOrderResponse(
    val id: Long,
    val profile: Profile,
    val animal: Animal,
    val title: String,
    val price: String,
    val subwayStation: SubwayStation,
    // date of order delivery formatted as dd.MM.yyyy - dd.MM.yyyy
    val serviceDate: String,
    // time of order delivery formatted as HH:mm (- HH:mm)
    val serviceTime: String = "",
    val description: String,
    // delivery type, in person or remote
    val serviceDeliveryType: ServiceDeliveryType,
)

