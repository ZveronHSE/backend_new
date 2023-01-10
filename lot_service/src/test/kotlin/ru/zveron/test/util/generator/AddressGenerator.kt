package ru.zveron.test.util.generator

import ru.zveron.contract.addressResponse
import ru.zveron.contract.lot.fullAddress
import ru.zveron.test.util.GeneratorUtils.generateDouble
import ru.zveron.test.util.GeneratorUtils.generateString

object AddressGenerator {

    fun generateAddress(id: Long) = addressResponse {
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
}