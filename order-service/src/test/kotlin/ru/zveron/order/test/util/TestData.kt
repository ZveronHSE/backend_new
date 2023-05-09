package ru.zveron.order.test.util

import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import ru.zveron.contract.address.internal.subwayStationInt
import ru.zveron.contract.profile.getProfileResponse
import ru.zveron.contract.profile.model.fullAnimal
import ru.zveron.order.client.address.dto.GetSubwayStationApiResponse
import ru.zveron.order.client.animal.dto.GetAnimalApiResponse
import ru.zveron.order.persistence.entity.OrderLot
import ru.zveron.order.persistence.repository.model.OrderLotWrapper
import ru.zveron.order.service.model.Animal
import ru.zveron.order.service.model.SubwayStation
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

fun testSubwayStation() = subwayStationInt {
    this.id = randomId().toInt()
    this.name = RandomStringUtils.randomAlphabetic(10)
    this.colorHex = RandomStringUtils.randomAlphabetic(8)
    this.town = RandomStringUtils.randomAlphabetic(10)
}

fun testFindProfileResponse() = getProfileResponse {
    this.id = randomId()
    this.name = randomName()
    this.imageUrl = RandomStringUtils.randomAlphabetic(10)
    this.addressId = randomId()
    this.surname = randomSurname()
}

fun testFullAnimal() = fullAnimal {
    this.id = randomId()
    this.name = randomName()
    this.imageUrls.addAll(listOf(RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(10)))
    this.age = RandomUtils.nextInt(1, 10)
    this.breed = RandomStringUtils.randomAlphabetic(10)
    this.species = RandomStringUtils.randomAlphabetic(10)
    this.documentUrls.addAll(listOf(RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(10)))
}

fun testOrderLotEntity() = OrderLot(
    id = randomId(),
    profileId = randomId(),
    animalId = randomId(),
    subwayId = randomId().toInt(),
    price = RandomUtils.nextLong(),
    title = RandomStringUtils.randomAlphabetic(10),
    description = RandomStringUtils.randomAlphabetic(10),
    serviceDateFrom = LocalDate.now(),
    serviceDateTo = LocalDate.now(),
    timeWindowFrom = LocalTime.now(),
    status = randomEnum(),
    serviceType = randomEnum(),
    serviceDeliveryType = randomEnum(),
    timeWindowTo = LocalTime.now(),
)

fun testOrderWrapper() = OrderLotWrapper(
    id = randomId(),
    animalId = randomId(),
    subwayId = randomId().toInt(),
    price = RandomUtils.nextLong(),
    title = RandomStringUtils.randomAlphabetic(10),
    serviceDateFrom = LocalDate.now(),
    serviceDateTo = LocalDate.now(),
    serviceType = randomEnum(),
    createdAt = Instant.now(),
)

fun testGetAnimalResponse() = GetAnimalApiResponse.Success(testFullAnimal())

fun testGetSubwayResponse() = GetSubwayStationApiResponse.Success(testSubwayStation())

fun testServiceSubwayStation() = SubwayStation(
    name = RandomStringUtils.randomAlphabetic(10),
    colorHex = RandomStringUtils.randomAlphabetic(8),
    town = RandomStringUtils.randomAlphabetic(10),
)

fun testServiceAnimal() = Animal(
    id = randomId(),
    name = randomName(),
    breed = RandomStringUtils.randomAlphabetic(10),
    species = RandomStringUtils.randomAlphabetic(10),
    imageUrl = randomImageUrl(),
)