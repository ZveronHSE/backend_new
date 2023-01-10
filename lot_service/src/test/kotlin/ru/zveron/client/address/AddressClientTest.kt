package ru.zveron.client.address

import io.grpc.Status
import io.grpc.StatusException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ru.zveron.contract.AddressServiceGrpcKt
import ru.zveron.contract.addressResponse
import ru.zveron.exception.LotException
import ru.zveron.mapper.LotMapper.toAddress
import ru.zveron.test.util.generator.AddressGenerator
import ru.zveron.test.util.GeneratorUtils

@ExtendWith(MockKExtension::class)
class AddressClientTest {
    @InjectMockKs
    lateinit var addressClient: AddressClient

    @MockK
    lateinit var addressStub: AddressServiceGrpcKt.AddressServiceCoroutineStub


    @Test
    fun `GetAddressById get correct answer for correct request`(): Unit = runBlocking {
        val id = GeneratorUtils.generateLong()
        val mock = AddressGenerator.generateAddress(id)

        coEvery {
            addressStub.getAddress(any(), any())
        } returns mock

        val responseExpected = mock.toAddress()
        val responseActual = addressClient.getAddressById(id)

        responseActual shouldBe responseExpected
    }

    @Test
    fun `GetAddressById throw exception, if dont find address by id`(): Unit = runBlocking {
        coEvery {
            addressStub.getAddress(any(), any())
        } throws StatusException(Status.NOT_FOUND)

        shouldThrow<LotException> { addressClient.getAddressById(GeneratorUtils.generateLong()) }
    }

    @Test
    fun `SaveAddressIfNotExists if saving old address, maybe get other address`(): Unit = runBlocking {
        val id = GeneratorUtils.generateLong()
        val request = AddressGenerator.generateFullAddress()
        val response = AddressGenerator.generateAddress(id)
        coEvery {
            addressStub.saveAddressIfNotExists(any(), any())
        } returns response

        val responseExpected = response.toAddress()
        val responseActual = addressClient.saveAddressIfNotExists(request)

        responseActual shouldBe responseExpected
    }

    @Test
    fun `SaveAddressIfNotExists if saving new address, get same address`(): Unit = runBlocking {
        val id = GeneratorUtils.generateLong()
        val request = AddressGenerator.generateFullAddress()
        val response = addressResponse {
            this.id = id
            region = request.region
            district = request.district
            town = request.town
            street = request.street
            house = request.house
            latitude = request.latitude
            longitude = request.longitude
        }

        coEvery {
            addressStub.saveAddressIfNotExists(any(), any())
        } returns response

        val responseExpected = response.toAddress()
        val responseActual = addressClient.saveAddressIfNotExists(request)

        responseActual shouldBe responseExpected
    }


    @Test
    fun `SaveAddressIfNotExists throw exception, if cant saving address`(): Unit = runBlocking {
        coEvery {
            addressStub.saveAddressIfNotExists(any(), any())
        } throws StatusException(Status.INTERNAL)

        shouldThrow<LotException> { addressClient.saveAddressIfNotExists(AddressGenerator.generateFullAddress()) }
    }
}