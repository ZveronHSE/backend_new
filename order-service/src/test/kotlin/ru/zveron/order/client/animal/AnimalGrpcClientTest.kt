package ru.zveron.order.client.animal

import io.grpc.Status
import io.grpc.StatusException
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.zveron.contract.profile.AnimalServiceInternalGrpcKt
import ru.zveron.contract.profile.getAnimalResponseInt
import ru.zveron.order.client.animal.dto.GetAnimalApiResponse
import ru.zveron.order.test.util.randomId
import ru.zveron.order.test.util.testFullAnimal

class AnimalGrpcClientTest {

    private val stub = mockk<AnimalServiceInternalGrpcKt.AnimalServiceInternalCoroutineStub>()

    private val client = AnimalGrpcClient(stub)

    @Test
    fun `given request, when stub returns response, then return success response`() {
        //prep data
        val animal = testFullAnimal()

        //prep env
        coEvery { stub.getAnimal(any(), any()) } returns getAnimalResponseInt { this.animal = animal }

        //when
        val response = runBlocking {
            client.getAnimal(randomId())
        }

        //then
        response.shouldBeTypeOf<GetAnimalApiResponse.Success>()
    }

    @Test
    fun `given request, when stub returns not found exception, then return not found response`() {
        //prep env
        coEvery { stub.getAnimal(any(), any()) } throws StatusException(Status.NOT_FOUND)

        //when
        val response = runBlocking {
            client.getAnimal(randomId())
        }

        //then
        response.shouldBeTypeOf<GetAnimalApiResponse.NotFound>()
    }

    @Test
    fun `given request, when stub returns unknown exception, then return error response`() {
        //prep env
        coEvery { stub.getAnimal(any(), any()) } throws StatusException(Status.UNKNOWN)

        //when
        val response = runBlocking {
            client.getAnimal(randomId())
        }

        //then
        response.shouldBeTypeOf<GetAnimalApiResponse.Error>()
    }
}
