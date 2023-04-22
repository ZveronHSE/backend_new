package ru.zveron.order.mapper.service

import ru.zveron.order.persistence.entity.OrderLot
import ru.zveron.order.service.constant.ServiceDeliveryType
import ru.zveron.order.service.dto.Animal
import ru.zveron.order.service.dto.GetOrderResponse
import ru.zveron.order.service.dto.Profile
import ru.zveron.order.service.dto.SubwayStation

fun mapToGetOrderResponse(o: OrderLot, subway: SubwayStation, profile: Profile, animal: Animal) = GetOrderResponse(
        id = o.id ?: error("Illegal entity state, id is null"),
        profile = profile,
        title = o.title,
        animal = animal,
        price = o.price,
        subwayStation = subway,
        serviceDateFrom = o.serviceDateFrom,
        serviceDateTo = o.serviceDateTo,
        timeWindowFrom = o.timeWindowFrom,
        timeWindowTo = o.timeWindowTo,
        description = o.description,
        serviceDeliveryType = ServiceDeliveryType.valueOf(o.serviceDeliveryType.name),
        orderStatus = o.status,
        createdAt = o.createdAt,
)
