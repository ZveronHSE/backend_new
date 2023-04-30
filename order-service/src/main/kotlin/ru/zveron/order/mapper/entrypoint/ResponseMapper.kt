package ru.zveron.order.mapper.entrypoint

import ru.zveron.contract.order.external.*
import ru.zveron.contract.order.model.AddressKt
import ru.zveron.contract.order.model.AnimalKt
import ru.zveron.contract.order.model.address
import ru.zveron.contract.order.model.animal
import ru.zveron.order.persistence.model.constant.Status
import ru.zveron.order.service.dto.Animal
import ru.zveron.order.service.dto.Profile
import ru.zveron.order.service.dto.SubwayStation
import ru.zveron.order.util.ChronoFormatter
import ru.zveron.order.util.PriceFormatter

@Suppress("unused")
fun GetOrderResponseKt.of(response: ru.zveron.order.service.dto.GetOrderResponse) = getOrderResponse {
    this.order = fullOrder {
        id = response.id
        profile = ProfileKt.of(response.profile)
        animal = AnimalKt.of(response.animal)
        address = AddressKt.of(response.subwayStation)
        description = response.description
        title = response.title
        serviceDate = ChronoFormatter.formatServiceDate(response.serviceDateFrom, response.serviceDateTo)
        price = PriceFormatter.formatToPrice(response.price)
        serviceTime = ChronoFormatter.formatServiceTime(response.timeWindowFrom, response.timeWindowTo)
        canAccept = Status.canAcceptOrder(response.orderStatus)
        createdAt = ChronoFormatter.formatCreatedAt(response.createdAt)
    }
}

fun ProfileKt.of(p: Profile) = profile {
    id = p.id
    name = p.name
    rating = p.rating.toFloat()
    imageUrl = p.imageUrl
}

fun AnimalKt.of(a: Animal) = animal {
    id = a.id
    name = a.name
    breed = a.breed
    species = a.species
    imageUrl = a.imageUrl
}

fun AddressKt.of(a: SubwayStation) = address {
    station = a.name
    town = a.town
    color = a.colorHex
}