package ru.zveron.order.test.util

import com.google.type.Date
import com.google.type.TimeOfDay
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import ru.zveron.contract.address.internal.subwayStationInt
import ru.zveron.contract.order.external.ServiceDeliveryMethod
import ru.zveron.contract.order.external.createOrderRequest
import ru.zveron.contract.order.model.ServiceType
import ru.zveron.contract.profile.getProfileResponse
import ru.zveron.contract.profile.model.fullAnimal
import ru.zveron.order.client.address.dto.GetSubwayStationApiResponse
import ru.zveron.order.client.animal.dto.GetAnimalApiResponse
import ru.zveron.order.persistence.entity.OrderLot
import ru.zveron.order.persistence.repository.model.OrderLotWrapper
import ru.zveron.order.service.model.Animal
import ru.zveron.order.service.model.CreateOrderRequest
import ru.zveron.order.service.model.Profile
import ru.zveron.order.service.model.SubwayStation
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

fun testSubwayStation() = subwayStationInt {
    this.id = RandomUtils.nextInt()
    this.name = RandomStringUtils.randomAlphabetic(10)
    this.colorHex = RandomStringUtils.randomAlphabetic(8)
    this.town = RandomStringUtils.randomAlphabetic(10)
}

fun testFindProfileResponse(id: Long = randomId()) = getProfileResponse {
    this.id = id
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
    serviceDeliveryType = randomEnum(),
)

fun testGetAnimalResponse() = GetAnimalApiResponse.Success(testFullAnimal())

fun testGetSubwayResponse() = GetSubwayStationApiResponse.Success(testSubwayStation())

fun testServiceSubwayStation() = SubwayStation(
    id = RandomUtils.nextInt(),
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

fun testCreateOrderRequest() = CreateOrderRequest(
    profileId = randomId(),
    subwayId = randomSubwayId(),
    animalId = randomId(),
    description = "description",
    price = RandomUtils.nextLong(),
    title = "title",
    serviceDateFrom = LocalDate.now(),
    serviceDateTo = LocalDate.now(),
    timeWindowFrom = LocalDate.now().atStartOfDay().toLocalTime(),
    timeWindowTo = LocalDate.now().atStartOfDay().toLocalTime(),
    serviceType = randomEnum(),
    serviceDeliveryType = randomEnum(),
)

fun testProfile() = Profile(
    id = randomId(),
    name = randomName(),
    imageUrl = randomImageUrl(),
    rating = 4.5,
)

fun testCreateOrderEntrypointRequest() = createOrderRequest {
    this.animalId = randomId()
    this.profileId = randomId()
    this.subwayStationId = randomSubwayId().toLong()

    this.price = RandomUtils.nextLong()
    this.title = RandomStringUtils.randomAlphabetic(10)
    this.description = RandomStringUtils.randomAlphabetic(10)

    this.deliveryMethod = ServiceDeliveryMethod.IN_PERSON
    this.serviceType = ServiceType.BOARDING

    this.serviceDateFrom = testProtoDate()
    this.serviceDateTo = testProtoDate()

    this.serviceTimeFrom = testTimeOfDay()
    this.serviceTimeTo = testTimeOfDay()
}

fun testProtoDate() = LocalDate.now().let {
    Date.newBuilder()
        .setDay(it.dayOfMonth)
        .setMonth(it.monthValue)
        .setYear(it.year)
        .build()
}

fun testTimeOfDay() = LocalTime.now().let {
    TimeOfDay.newBuilder()
        .setHours(it.hour)
        .setMinutes(it.minute)
        .setSeconds(it.second)
        .build()
}
