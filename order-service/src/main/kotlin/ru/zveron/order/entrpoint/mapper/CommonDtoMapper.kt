package ru.zveron.order.entrpoint.mapper

import ru.zveron.contract.order.external.ProfileKt
import ru.zveron.contract.order.external.WaterfallOrderKt
import ru.zveron.contract.order.external.profile
import ru.zveron.contract.order.external.waterfallOrder
import ru.zveron.contract.order.model.AddressKt
import ru.zveron.contract.order.model.AnimalKt
import ru.zveron.contract.order.model.address
import ru.zveron.contract.order.model.animal
import ru.zveron.order.service.model.Profile
import ru.zveron.order.service.model.SubwayStation
import ru.zveron.order.service.model.WaterfallOrderLot
import ru.zveron.order.util.ChronoFormatter
import ru.zveron.order.util.PriceFormatter

@Suppress("unused")
object CommonDtoMapper {

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

    fun WaterfallOrderKt.of(wo: WaterfallOrderLot) = waterfallOrder {
        id = wo.id
        animal = AnimalKt.of(wo.animal)
        title = wo.title
        wo.subway?.let { address = AddressKt.of(it) }
        serviceDate = ChronoFormatter.formatServiceDate(wo.serviceDateFrom, wo.serviceDateTo)
        createdAt = ChronoFormatter.formatCreatedAt(wo.createdAt)
        price = PriceFormatter.formatToPrice(wo.price.toString())
    }
}
