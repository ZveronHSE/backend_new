package ru.zveron.mapper

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

import ru.zveron.contract.addressRequest
import ru.zveron.contract.addressResponse
import ru.zveron.mapper.AddressMapper.toEntity
import ru.zveron.mapper.AddressMapper.toResponse
import ru.zveron.util.CreateEntitiesUtil.mockAddressEntity

class AddressMapperTest {

    @Test
    fun `Mapping from address entity to address response`() {
        val expectedResponse = addressResponse {
            id = 1
            region = "region"
            district = "district"
            town = "town"
            street = "street"
            house = "house"
            longitude = 10.0
            latitude = 20.0
        }
        val actualResponse = mockAddressEntity(1).toResponse()

        actualResponse shouldBe expectedResponse
    }

    @Test
    fun `Mapping from address request to address entity`() {
        val expectedEntity = mockAddressEntity(0)
        val actualEntity = addressRequest {
            region = "region"
            district = "district"
            town = "town"
            street = "street"
            house = "house"
            longitude = 10.0
            latitude = 20.0
        }.toEntity()

        actualEntity shouldBe expectedEntity
    }
}