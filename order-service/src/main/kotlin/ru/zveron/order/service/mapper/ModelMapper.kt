package ru.zveron.order.service.mapper

import ru.zveron.contract.address.internal.SubwayStationInt
import ru.zveron.contract.profile.GetProfileResponse
import ru.zveron.contract.profile.model.FullAnimal
import ru.zveron.order.service.model.Animal
import ru.zveron.order.service.model.Profile
import ru.zveron.order.service.model.SubwayStation

@Suppress("unused")
object ModelMapper {
    fun SubwayStation.Companion.of(s: SubwayStationInt) = SubwayStation(
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
}