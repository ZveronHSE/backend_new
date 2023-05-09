package ru.zveron.order.entrpoint.mapper

import ru.zveron.contract.order.external.*
import ru.zveron.contract.order.model.AddressKt
import ru.zveron.contract.order.model.AnimalKt
import ru.zveron.contract.order.model.address
import ru.zveron.contract.order.model.animal
import ru.zveron.order.persistence.model.constant.Status
import ru.zveron.order.service.model.Profile
import ru.zveron.order.service.model.SubwayStation
import ru.zveron.order.service.model.WaterfallOrderLot
import ru.zveron.order.util.ChronoFormatter
import ru.zveron.order.util.PriceFormatter

@Suppress("unused")
object ResponseMapper {
    fun GetOrderResponseKt.of(response: ru.zveron.order.service.model.GetOrderResponse) = getOrderResponse {
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

    fun AnimalKt.of(a: ru.zveron.order.service.model.Animal) = animal {
        id = a.id
        name = a.name
        breed = a.breed
        species = a.species
        imageUrl = a.imageUrl
    }

    fun AddressKt.of(s: SubwayStation) = address {
        station = s.name
        town = s.town
        color = s.colorHex
    }

    fun ProfileKt.of(p: Profile) = profile {
        id = p.id
        name = p.name
        rating = p.rating.toFloat()
    }

    fun GetWaterfallResponseKt.of(waterfallOrderLots: List<WaterfallOrderLot>) = getWaterfallResponse {
        this.orders.addAll(
            waterfallOrderLots.map { WaterfallOrderKt.of(it) }
        )
    }

    fun WaterfallOrderKt.of(wo: WaterfallOrderLot) = waterfallOrder {
        this.id = wo.id
        this.animal = AnimalKt.of(wo.animal)
        this.title = wo.title
        this.address = AddressKt.of(wo.subway)
        this.serviceDate = ChronoFormatter.formatServiceDate(wo.serviceDateFrom, wo.serviceDateTo)
        this.createdAt = ChronoFormatter.formatCreatedAt(wo.createdAt)
        this.price = PriceFormatter.formatToPrice(wo.price.toString())
    }
}
