package ru.zveron.client.profile

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
import ru.zveron.contract.profile.ProfileServiceInternalGrpcKt
import ru.zveron.exception.LotException
import ru.zveron.mapper.LotMapper.toAddress
import ru.zveron.mapper.SellerMapper.toSellerProfile
import ru.zveron.test.util.GeneratorUtils
import ru.zveron.test.util.generator.ProfileGenerator

@ExtendWith(MockKExtension::class)
class ProfileClientTest {
    @InjectMockKs
    lateinit var profileClient: ProfileClient

    @MockK
    lateinit var profileStub: ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub

    @Test
    fun `GetProfileWithContacts get correct answer for correct request`(): Unit = runBlocking {
        val sellerId = GeneratorUtils.generateLong()
        val response = ProfileGenerator.generateProfileWithContacts(sellerId)

        coEvery {
            profileStub.getProfileWithContacts(any(), any())
        } returns response

        val responseExpected = response.toSellerProfile()
        val responseActual = profileClient.getProfileWithContacts(sellerId)

        responseActual shouldBe responseExpected
    }

    @Test
    fun `GetAddressById throw exception, if dont find address by id`(): Unit = runBlocking {
        coEvery {
            profileStub.getProfileWithContacts(any(), any())
        } throws StatusException(Status.NOT_FOUND)

        shouldThrow<LotException> { profileClient.getProfileWithContacts(GeneratorUtils.generateLong()) }
    }
}