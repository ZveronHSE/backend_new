package ru.zveron.order.mapper.entrypoint

import ru.zveron.contract.order.external.GetOrderResponseKt
import ru.zveron.contract.order.external.ServiceDeliveryMethod
import ru.zveron.contract.order.external.fullOrder
import ru.zveron.contract.order.external.getOrderResponse
import ru.zveron.contract.order.external.profile
import ru.zveron.contract.order.model.address
import ru.zveron.contract.order.model.animal

@Suppress("unused")
fun GetOrderResponseKt.of(response: ru.zveron.order.service.dto.GetOrderResponse) = getOrderResponse {
    this.order = fullOrder {
        id = response.id
        profile = profile {
            id = response.profile.id
            name = response.profile.name
            rating = response.profile.rating.toFloat()
        }
        animal = animal {
            id = response.animal.id
            name = response.animal.name
            breed = response.animal.breed
            species = response.animal.species
            imageUrl = response.animal.imageUrl
        }
        address = address {
            station = response.subwayStation.name
            town = response.subwayStation.town
            color = response.subwayStation.colorHex
        }
        description = response.description
        title = response.title
        serviceDate = response.serviceDate
        price = response.price
        serviceTime = response.serviceTime
        serviceDeliveryMethod = ServiceDeliveryMethod.valueOf(response.serviceDeliveryType.name)
    }
}