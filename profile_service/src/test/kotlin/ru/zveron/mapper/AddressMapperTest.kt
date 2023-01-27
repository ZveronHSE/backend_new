package ru.zveron.mapper

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import ru.zveron.commons.assertions.addressShouldBe
import ru.zveron.commons.generator.AddressGenerator
import ru.zveron.commons.generator.PropsGenerator
import ru.zveron.mapper.AddressMapper.toAddress
import ru.zveron.mapper.AddressMapper.toProfileAddress
import ru.zveron.mapper.AddressMapper.toRequest

class AddressMapperTest {

    @Test
    fun `Map to profile address if town not blank`() {
        val address = AddressGenerator.generateAddress(PropsGenerator.generateUserId())

        address.toProfileAddress() shouldBe "г. ${address.town}"
    }

    @Test
    fun `Map to profile address if town is blank`() {
        val address = AddressGenerator.generateAddress(PropsGenerator.generateUserId(), blankTown = true)

        address.toProfileAddress() shouldBe address.region
    }

    @Test
    fun `Map address to address request`() {
        val expectedAddress = AddressGenerator.generateAddress()

        val actualAddress = expectedAddress.toRequest()

        actualAddress addressShouldBe expectedAddress
    }

    @Test
    fun `Map address response to address`() {
        val expectedAddress = AddressGenerator.generateAddress(PropsGenerator.generateUserId())

        val actualAddress = expectedAddress.toAddress()

        actualAddress addressShouldBe expectedAddress
    }
}