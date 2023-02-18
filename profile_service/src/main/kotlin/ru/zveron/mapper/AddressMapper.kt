package ru.zveron.mapper

import ru.zveron.contract.profile.Address
import ru.zveron.contract.profile.address
import ru.zveron.contract.address.AddressResponse
import ru.zveron.contract.address.addressRequest

object AddressMapper {

    fun AddressResponse.toProfileAddress(): String = if (town.isNullOrBlank()) {
        region
    } else {
        "г. $town"
    }

    fun Address.toRequest() = addressRequest {
        region = this@toRequest.region
        town = this@toRequest.town
        longitude = this@toRequest.longitude
        latitude = this@toRequest.latitude
    }

    fun AddressResponse.toAddress(): Address =
        address {
            region = this@toAddress.region
            town = this@toAddress.town
            longitude = this@toAddress.longitude
            latitude = this@toAddress.latitude
        }
}