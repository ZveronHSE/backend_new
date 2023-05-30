package ru.zveron.service.mapper

import org.jooq.Record
import org.jooq.TableField
import ru.zveron.contract.address.internal.SubwayStationInt
import ru.zveron.contract.profile.GetProfileResponse
import ru.zveron.contract.profile.model.FullAnimal
import ru.zveron.persistence.entity.OrderLot
import ru.zveron.persistence.model.constant.Status
import ru.zveron.service.model.CreateOrderRequest
import ru.zveron.service.model.Animal
import ru.zveron.service.model.FilterParam
import ru.zveron.service.model.Profile
import ru.zveron.service.model.SubwayStation

@Suppress("unused")
object ModelMapper {
    fun SubwayStation.Companion.of(s: SubwayStationInt) = SubwayStation(
        id = s.id,
        town = s.town,
        name = s.name,
        colorHex = s.colorHex,
    )

    fun Animal.Companion.of(a: FullAnimal) = Animal(
        id = a.id,
        name = a.name,
        imageUrl = a.imageUrlsList.first(),
        species = a.species,
        breed = a.breed,
    )

    fun Profile.Companion.of(p: GetProfileResponse, rating: Double) = Profile(
        id = p.id,
        name = p.name,
        imageUrl = p.imageUrl,
        rating = rating,
    )

    @Suppress("UNCHECKED_CAST")
    fun FilterParam.toJooqFilter() = operation.operator(field.tableField as TableField<Record, Any>, value)

    fun CreateOrderRequest.toOrderLot() = OrderLot(
        profileId = profileId,
        animalId = animalId,
        subwayId = subwayId,
        description = description,
        price = price,
        title = title,
        serviceDateFrom = serviceDateFrom,
        serviceDateTo = serviceDateTo,
        timeWindowFrom = timeWindowFrom,
        timeWindowTo = timeWindowTo,
        serviceType = serviceType,
        serviceDeliveryType = serviceDeliveryType,
        status = Status.PENDING,
    )
}