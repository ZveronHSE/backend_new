package ru.zveron.service

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.DataBaseApplicationTest
import ru.zveron.grpc.AddressEntrypoint
import ru.zveron.mapper.AddressMapper.toResponse
import ru.zveron.repository.AddressRepository
import ru.zveron.util.CreateEntitiesUtil.mockAddressEntity
import ru.zveron.util.CreateEntitiesUtil.mockAddressIdRequest
import ru.zveron.util.CreateEntitiesUtil.mockAddressRequest
import javax.persistence.EntityNotFoundException

class AddressEntrypointTest : DataBaseApplicationTest() {
    @Autowired
    lateinit var addressRepository: AddressRepository

    @Autowired
    lateinit var addressEntrypoint: AddressEntrypoint

    companion object {
        const val MOCK_ADDRESS_ID = 1L
    }

    @Test
    fun `GetAddress get address by id if exists`(): Unit = runBlocking {
        val entity = mockAddressEntity(id = MOCK_ADDRESS_ID)
        addressRepository.save(entity)

        val actualResponse = addressEntrypoint.getAddress(mockAddressIdRequest(MOCK_ADDRESS_ID))

        actualResponse shouldBe entity.toResponse()
    }

    @Test
    fun `GetAddress should throw exception if dont find address by id`(): Unit = runBlocking {
        shouldThrow<EntityNotFoundException> {
            addressEntrypoint.getAddress(mockAddressIdRequest(MOCK_ADDRESS_ID))
        }
    }


    @Test
    fun `SaveAddressIfNotExists save new address`(): Unit = runBlocking {
        val request = mockAddressRequest()
        val response = addressEntrypoint.saveAddressIfNotExists(request)

        response.asClue {
            it.house shouldBe request.house
            it.street shouldBe request.street
            it.longitude shouldBe request.longitude
            it.latitude shouldBe request.latitude
        }
    }

    @Test
    fun `SaveAddressIfNotExists get old address, if exists by longitude and latitude`(): Unit = runBlocking {
        val request = mockAddressRequest(region = "region")
        val request1 = mockAddressRequest(region = "region1")
        addressEntrypoint.saveAddressIfNotExists(request)
        val response = addressEntrypoint.saveAddressIfNotExists(request1)


        response.asClue {
            it.house shouldBe request.house
            it.street shouldBe request.street
            it.longitude shouldBe request.longitude
            it.latitude shouldBe request.latitude
        }
    }
}