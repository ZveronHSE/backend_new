package ru.zveron.order.mapper.service

import ru.zveron.contract.address.internal.SubwayStationInt
import ru.zveron.contract.profile.GetProfileResponse
import ru.zveron.contract.profile.model.FullAnimal
import ru.zveron.order.service.dto.Animal
import ru.zveron.order.service.dto.Profile
import ru.zveron.order.service.dto.SubwayStation

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
