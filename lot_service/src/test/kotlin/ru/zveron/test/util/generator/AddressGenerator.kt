package ru.zveron.test.util.generator

import ru.zveron.contract.address.addressResponse
import ru.zveron.contract.lot.fullAddress
import ru.zveron.model.Address
import ru.zveron.test.util.GeneratorUtils.generateDouble
import ru.zveron.test.util.GeneratorUtils.generateString

object AddressGenerator {

    fun generateAddressResponse(id: Long) = addressResponse {
        this.id = id
        region = generateString(10)
        district = generateString(10)
        town = generateString(10)
        street = generateString(10)
        house = generateString(10)
        latitude = generateDouble()
        longitude = generateDouble()
    }

    fun generateFullAddress() = fullAddress {
        region = generateString(10)
        district = generateString(10)
        town = generateString(10)
        street = generateString(10)
        house = generateString(10)
        latitude = generateDouble()
        longitude = generateDouble()
    }

    fun generateAddress(id: Long) = Address(
        id = id,
        address = "г. Москва, Покровская улица, д. 11",
        latitude = generateDouble(),
        longitude = generateDouble()
    )
}