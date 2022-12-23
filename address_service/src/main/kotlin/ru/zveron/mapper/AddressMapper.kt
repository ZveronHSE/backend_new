package ru.zveron.mapper

import ru.zveron.contract.AddressRequest
import ru.zveron.contract.AddressResponse
import ru.zveron.contract.addressResponse
import ru.zveron.entity.Address

object AddressMapper {
    fun Address.toResponse(): AddressResponse {
        val address = this

        return addressResponse {
            id = address.id
            address.region?.let { region = it }
            address.district?.let { district = it }
            address.town?.let { town = it }
            street = address.street
            house = address.house
            longitude = address.longitude
            latitude = address.latitude
        }
    }

    fun AddressRequest.toEntity(): Address {
        return Address(
            region = region,
            district = district,
            town = town,
            street = street,
            house = house,
            longitude = longitude,
            latitude = latitude
        )
    }
}