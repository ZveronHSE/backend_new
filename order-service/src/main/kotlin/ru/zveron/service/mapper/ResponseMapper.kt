package ru.zveron.service.mapper

import ru.zveron.persistence.entity.OrderLot
import ru.zveron.persistence.entity.Statistics
import ru.zveron.persistence.repository.model.OrderLotWrapper
import ru.zveron.service.constant.ServiceDeliveryType
import ru.zveron.service.model.Animal
import ru.zveron.service.model.CustomerProfileOrder
import ru.zveron.service.model.FullOrderData
import ru.zveron.service.model.GetCustomerResponse
import ru.zveron.service.model.Profile
import ru.zveron.service.model.ProfileOrder
import ru.zveron.service.model.SubwayStation
import ru.zveron.service.model.WaterfallOrderLot
import ru.zveron.util.PriceFormatter

object ResponseMapper {
    fun mapToFullOrderData(o: OrderLot, subway: SubwayStation?, profile: Profile, animal: Animal) = FullOrderData(
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
    ): List<WaterfallOrderLot> =
        orderLotRecords.mapNotNull {
            animals[it.animalId]?.let { animal ->
                WaterfallOrderLot(
                    id = it.id,
                    animal = animal,
                    title = it.title,
                    subway = subwayStations[it.subwayId],
                    createdAt = it.createdAt,
                    serviceDateFrom = it.serviceDateFrom,
                    serviceDateTo = it.serviceDateTo,
                    price = it.price,
                )
            }
        }

    fun toGetCustomerResponse(
        profile: Profile,
        orderLots: List<OrderLot> = emptyList(),
        subwayStations: Map<Int, SubwayStation?> = emptyMap(),
        animals: Map<Long, Animal?> = emptyMap(),
    ): GetCustomerResponse {
        val customerOrderLots = orderLots.mapNotNull {
            animals[it.animalId]?.let { animal ->
                CustomerProfileOrder(
                    id = it.id ?: error("Illegal entity state, id is null"),
                    animal = animal,
                    price = it.price,
                    title = it.title,
                    subway = subwayStations[it.subwayId],
                    createdAt = it.createdAt,
                    serviceDateFrom = it.serviceDateFrom,
                    serviceDateTo = it.serviceDateTo,
                    status = it.status,
                )
            }
        }

        return GetCustomerResponse(
            profile = profile,
            orderLots = customerOrderLots,
        )
    }

    fun mapToProfileOrders(
        orderLots: List<OrderLot> = emptyList(),
        animals: List<Animal> = emptyList(),
        statisticsList: List<Statistics>,
    ): List<ProfileOrder> {
        val animalIdToAnimal = animals.associateBy { it.id }
        val orderIdToViewCount = statisticsList.associate { it.orderLotId to it.viewCount }

        return orderLots.mapNotNull {
            animalIdToAnimal[it.animalId]?.let { animal ->
                ProfileOrder(
                    orderLotId = it.id ?: error("Illegal entity state, id is null"),
                    title = it.title,
                    price = PriceFormatter.formatToPrice(it.price),
                    imageUrl = animal.imageUrl,
                    viewCount = orderIdToViewCount[it.id] ?: 0,
                    status = it.status,
                    animal = animal,
                    createdAt = it.createdAt,
                    serviceDateTo = it.serviceDateTo,
                    serviceDateFrom = it.serviceDateFrom,
                )
            }
        }
    }
}
