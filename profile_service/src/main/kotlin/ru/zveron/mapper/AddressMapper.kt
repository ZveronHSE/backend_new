package ru.zveron.mapper

import ru.zveron.Address
import ru.zveron.address
import ru.zveron.contract.AddressResponse
import ru.zveron.contract.addressRequest

object AddressMapper {

    fun AddressResponse.toProfileAddress(): String = if (town.isNullOrBlank()) {
        region
    } else {
        "г. $town"
    }

    fun address2Request(address: Address) = addressRequest {
        region = address.region
        town = address.town
        longitude = address.longitude
        latitude = address.latitude
    }

    fun response2Address(response: AddressResponse): Address =
        address {
            region = response.region
            town = response.town
            longitude = response.longitude
            latitude = response.latitude
        }
}