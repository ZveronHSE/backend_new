package ru.zveron.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.DataBaseApplicationTest
import ru.zveron.mapper.AddressMapper.toResponse
import ru.zveron.repository.AddressRepository
import ru.zveron.util.CreateEntitiesUtil.mockAddressEntity
import ru.zveron.util.CreateEntitiesUtil.mockAddressRequest
import javax.persistence.EntityNotFoundException

class AddressServiceTest : DataBaseApplicationTest() {
    @Autowired
    lateinit var addressRepository: AddressRepository

    @Autowired
    lateinit var addressService: AddressService

    companion object {
        const val MOCK_ADDRESS_ID = 1L
    }

    @Test
    fun `GetAddress get address by id if exists`(): Unit = runBlocking {
        val entity = mockAddressEntity(id = MOCK_ADDRESS_ID)
        addressRepository.save(entity)

        val actualResponse = addressService.getAddress(mockAddressRequest(MOCK_ADDRESS_ID))

        actualResponse shouldBe entity.toResponse()
    }

    @Test
    fun `GetAddress should throw exception if dont find address by id`(): Unit = runBlocking {
        shouldThrow<EntityNotFoundException> {
            addressService.getAddress(mockAddressRequest(MOCK_ADDRESS_ID))
        }
    }


    @Test
    fun `SaveAddressIfNotExists save new address`(): Unit = runBlocking {

    }
    //    override suspend fun saveAddressIfNotExists(request: AddressRequest): AddressResponse {
    //        val address = if (addressRepository.existsByLongitudeAndLatitude(request.longitude, request.latitude)) {
    //            addressRepository.getByLongitudeAndLatitude(request.longitude, request.latitude)
    //        } else {
    //            addressRepository.save(request.toEntity())
    //        }
    //
    //        return address.toResponse()
    //    }
}