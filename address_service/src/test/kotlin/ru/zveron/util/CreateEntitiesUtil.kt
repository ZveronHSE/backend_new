package ru.zveron.util

import ru.zveron.contract.addressIdRequest
import ru.zveron.entity.Address

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

    fun mockAddressRequest(id: Long) = addressIdRequest {
        this.id = id
    }
}