package ru.zveron.order.util

import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import ru.zveron.contract.address.internal.subwayStationInt
import ru.zveron.contract.profile.getProfileResponse
import ru.zveron.contract.profile.model.fullAnimal
import ru.zveron.order.persistence.entity.OrderLot
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
    price = RandomUtils.nextInt().toString(),
    title = RandomStringUtils.randomAlphabetic(10),
    description = RandomStringUtils.randomAlphabetic(10),
    serviceDateFrom = LocalDate.now(),
    serviceDateTo = LocalDate.now(),
    serviceTime = LocalTime.now(),
    status = randomEnum(),
    type = randomEnum(),
    serviceDeliveryType = randomEnum(),
)