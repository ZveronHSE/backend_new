package ru.zveron.order.entrpoint.mapper

import com.google.type.Date
import com.google.type.TimeOfDay
import ru.zveron.contract.order.external.ProfileKt
import ru.zveron.contract.order.external.WaterfallOrderKt
import ru.zveron.contract.order.external.profile
import ru.zveron.contract.order.external.waterfallOrder
import ru.zveron.contract.order.model.Address
import ru.zveron.contract.order.model.AddressKt
import ru.zveron.contract.order.model.AnimalKt
import ru.zveron.contract.order.model.address
import ru.zveron.contract.order.model.animal
import ru.zveron.order.service.model.Profile
import ru.zveron.order.service.model.SubwayStation
import ru.zveron.order.service.model.WaterfallOrderLot
import ru.zveron.order.util.ChronoFormatter
import ru.zveron.order.util.PriceFormatter
import java.time.LocalDate
import java.time.LocalTime

@Suppress("unused")
object CommonDtoMapper {

    fun AnimalKt.of(a: ru.zveron.order.service.model.Animal) = animal {
        id = a.id
        name = a.name
        breed = a.breed
        species = a.species
        imageUrl = a.imageUrl
    }

    fun AddressKt.of(s: SubwayStation?): Address {
        return if (s == null) address {
            station = ""
            town = ""
            color = ""
        } else
            address {
                station = s.name
                town = s.town
                color = s.colorHex
            }
    }

    fun ProfileKt.of(p: Profile) = profile {
        id = p.id
        name = p.name
        rating = p.rating.toFloat()
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

    fun Date.toLocalDate() = LocalDate.of(this.year, this.month, this.day)

    fun TimeOfDay.toLocalTime() = LocalTime.of(this.hours, this.minutes, this.seconds)
}
