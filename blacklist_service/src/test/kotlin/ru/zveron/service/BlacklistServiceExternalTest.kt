package ru.zveron.service

import com.google.protobuf.empty
import io.grpc.Status
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import ru.zveron.BlacklistTest
import ru.zveron.client.profile.ProfileClient
import ru.zveron.commons.Assertions.summaryShouldBe
import ru.zveron.commons.BlacklistServiceEntitiesGenerator.createAddToBlacklistRequest
import ru.zveron.commons.BlacklistServiceEntitiesGenerator.createDeleteFromBlacklistRequest
import ru.zveron.commons.BlacklistServiceEntitiesGenerator.generateBlacklistRecord
import ru.zveron.commons.BlacklistServiceEntitiesGenerator.generateNIds
import ru.zveron.commons.BlacklistServiceEntitiesGenerator.generateProfileSummary
import ru.zveron.commons.BlacklistServiceEntitiesGenerator.generateUserId
import ru.zveron.config.AuthorizedProfileElement
import ru.zveron.contract.profile.getProfilesSummaryResponse
import ru.zveron.entity.BlacklistRecord
import ru.zveron.exception.BlacklistException
import ru.zveron.repository.BlacklistRepository

@Suppress("BlockingMethodInNonBlockingContext")
class BlacklistServiceExternalTest : BlacklistTest() {

    @Autowired
    lateinit var blacklistRepository: BlacklistRepository

    @Autowired
    lateinit var blacklistService: BlacklistServiceExternal

    @TestConfiguration
    class InternalConfiguration {
        @Bean
        fun profileClient() = mockk<ProfileClient>()
    }

    @Autowired
    lateinit var profileClient: ProfileClient

    @Test
    fun `GetBlacklist When get list of records got records`() {
        val (user1Id, user2Id, user3Id) = generateNIds(3)
        val profile2 = generateProfileSummary(user2Id)
        val profile3 = generateProfileSummary(user3Id)
        coEvery { profileClient.getProfilesSummary(listOf(user2Id, user3Id)) } returns getProfilesSummaryResponse {
            profiles.addAll(listOf(profile2, profile3))
        }
        runBlocking(AuthorizedProfileElement(user1Id)) {
            val record1 = blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user1Id, user2Id)))
            val record2 = blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user1Id, user3Id)))
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user2Id, user3Id)))

            val set = blacklistService.getBlacklist(empty { }).blacklistUsersList.toSet()

            set.apply {
                size shouldBe 2
                first { it.id ==  user2Id} summaryShouldBe profile2
                first { it.id ==  user3Id} summaryShouldBe profile3
            }
            set.map { it.id }.shouldContainExactlyInAnyOrder(record1.id.reportedUserId, record2.id.reportedUserId)
        }
    }

    @Test
    fun `GetBlacklist When unauthenticated`() {
        val exception = shouldThrow<BlacklistException> {
            runBlocking {
                blacklistService.getBlacklist(empty { })
            }
        }

        exception.status shouldBe Status.UNAUTHENTICATED
        exception.message shouldBe "Authentication required"
    }

    @Test
    fun `AddToBlacklist When add someone to blacklist new record is created`() {
        val (user1Id, user2Id) = generateNIds(2)
        runBlocking(AuthorizedProfileElement(user1Id)) {
            blacklistService.addToBlacklist(createAddToBlacklistRequest(user2Id))

            blacklistRepository.findById(generateBlacklistRecord(user1Id, user2Id).id).isPresent shouldBe true
        }
    }

    @Test
    fun `AddToBlacklist When add someone to blacklist while it is in the black list no exceptions are thrown`() {
        val (user1Id, user2Id) = generateNIds(2)
        shouldNotThrow<BlacklistException> {
            runBlocking(AuthorizedProfileElement(user1Id)) {
                blacklistRepository.save(generateBlacklistRecord(user1Id, user2Id))

                blacklistService.addToBlacklist(createAddToBlacklistRequest(user2Id))
            }
        }
    }

    @Test
    fun `AddToBlacklist When add myself to blacklist got exception`() {
        val user1Id = generateUserId()
        val exception = shouldThrow<BlacklistException> {
            runBlocking(AuthorizedProfileElement(user1Id)) {
                blacklistService.addToBlacklist(createAddToBlacklistRequest(user1Id))
            }
        }

        exception.message shouldBe "Нельзя добавить себя в черный список"
    }

    @Test
    fun `AddToBlacklist When unauthenticated`() {
        val user1Id = generateUserId()
        val exception = shouldThrow<BlacklistException> {
            runBlocking {
                blacklistService.addToBlacklist(createAddToBlacklistRequest(user1Id))
            }
        }

        exception.status shouldBe Status.UNAUTHENTICATED
        exception.message shouldBe "Authentication required"
    }

    @Test
    fun `DeleteFromBlacklist When delete someone from blacklist new record is deleted`() {
        val (user1Id, user2Id) = generateNIds(2)
        runBlocking(AuthorizedProfileElement(user1Id)) {
            val record = blacklistRepository.save(generateBlacklistRecord(user1Id, user2Id))

            blacklistService.deleteFromBlacklist(createDeleteFromBlacklistRequest(user2Id))

            blacklistRepository.findById(record.id).isEmpty shouldBe true
        }
    }

    @Test
    fun `DeleteFromBlacklist When delete someone from blacklist while it is not in the black list no exceptions are thrown`() {
        val (user1Id, user2Id) = generateNIds(2)
        shouldNotThrow<BlacklistException> {
            runBlocking(AuthorizedProfileElement(user1Id)) {
                blacklistService.deleteFromBlacklist(createDeleteFromBlacklistRequest(user2Id))
            }
        }
    }

    @Test
    fun `DeleteFromBlacklist When delete myself from blacklist got exception`() {
        val user1Id = generateUserId()
        val exception = shouldThrow<BlacklistException> {
            runBlocking(AuthorizedProfileElement(user1Id)) {
                blacklistService.deleteFromBlacklist(createDeleteFromBlacklistRequest(user1Id))
            }
        }

        exception.message shouldBe "Нельзя удалить себя из черного списка"
    }

    @Test
    fun `DeleteFromBlacklist When unauthenticated`() {
        val user1Id = generateUserId()
        val exception = shouldThrow<BlacklistException> {
            runBlocking {
                blacklistService.deleteFromBlacklist(createDeleteFromBlacklistRequest(user1Id))
            }
        }

        exception.status shouldBe Status.UNAUTHENTICATED
        exception.message shouldBe "Authentication required"
    }
}