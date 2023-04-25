package ru.zveron.util

import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import ru.zveron.contract.address.addressIdRequest
import ru.zveron.contract.address.addressRequest
import ru.zveron.contract.address.external.getSubwayStationsByCityExtRequest
import ru.zveron.contract.address.internal.getSubwayStationRequest
import ru.zveron.contract.address.internal.getSubwayStationsRequest
import ru.zveron.entity.Address
import ru.zveron.entity.SubwayStation

object CreateEntitiesUtil {
    fun mockAddressEntity(id: Long) = Address(
        id = id,
        region = "region",
        district = "district",
        town = "town",
        street = "street",
        house = "house",
        longitude = 10.0,
        latitude = 20.0
    )

    fun mockAddressIdRequest(id: Long) = addressIdRequest {
        this.id = id
    }

    fun mockAddressRequest(region: String = "region") = addressRequest {
        this.region = region
        district = "district"
        town = "town"
        street = "street"
        house = "house"
        longitude = 10.0
        latitude = 20.0
    }

    fun testGetSubwayByCityRequest(city: String = "city") = getSubwayStationsByCityExtRequest {
        this.city = city
    }

    fun testGetSubwayByIdRequest(id: Int = RandomUtils.nextInt()) = getSubwayStationRequest {
        this.id = id
    }

    fun testGetSubwayStationsRequest(ids: List<Int> = listOf(RandomUtils.nextInt())) = getSubwayStationsRequest {
        this.ids.addAll(ids)
    }

    fun testSubwayStation() = SubwayStation(
        name = RandomStringUtils.randomAlphabetic(10),
        city = RandomStringUtils.randomAlphabetic(10),
        colorHex = RandomStringUtils.randomAlphabetic(8)
    )
}