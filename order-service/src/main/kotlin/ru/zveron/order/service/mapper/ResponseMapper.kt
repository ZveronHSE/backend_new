package ru.zveron.order.service.mapper

import ru.zveron.order.persistence.entity.OrderLot
import ru.zveron.order.persistence.repository.model.OrderLotWrapper
import ru.zveron.order.service.constant.ServiceDeliveryType
import ru.zveron.order.service.model.Animal
import ru.zveron.order.service.model.GetOrderResponse
import ru.zveron.order.service.model.Profile
import ru.zveron.order.service.model.SubwayStation
import ru.zveron.order.service.model.WaterfallOrderLot

object ResponseMapper {
    fun mapToGetOrderResponse(o: OrderLot, subway: SubwayStation?, profile: Profile, animal: Animal) = GetOrderResponse(
        id = o.id ?: error("Illegal entity state, id is null"),
        profile = profile,
        title = o.title,
        animal = animal,
        price = o.price.toString(),
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

    fun toGetOrderWaterfallResponse(
        orderLotRecords: List<OrderLotWrapper>,
        subwayStations: Map<Int, SubwayStation?>,
        animals: Map<Long, Animal?>,
    ) =
        orderLotRecords.map {
            if (animals[it.animalId] == null) {
                return@map null
            }
            WaterfallOrderLot(
                id = it.id,
                animal = animals[it.animalId] ?: error("Animal should be present"),
                title = it.title,
                subway = subwayStations[it.subwayId],
                createdAt = it.createdAt,
                serviceDateFrom = it.serviceDateFrom,
                serviceDateTo = it.serviceDateTo,
                price = it.price,
            )
        }.filterNotNull()
}