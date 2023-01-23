package ru.zveron.commons.generator

import ru.zveron.address
import ru.zveron.commons.generator.PropsGenerator.generateDouble
import ru.zveron.commons.generator.PropsGenerator.generateString
import ru.zveron.contract.addressResponse

object AddressGenerator {

    fun generateAddress(id: Long, blankTown: Boolean = false) = addressResponse {
        this.id = id
        region = generateString(10)
        district = generateString(10)
        town = if (blankTown) "" else generateString(10)
        street = generateString(10)
        house = generateString(10)
        latitude = generateDouble()
        longitude = generateDouble()
    }

    fun generateAddress() = address {
        region = generateString(10)
        town = generateString(10)
        latitude = generateDouble()
        longitude = generateDouble()
    }
}