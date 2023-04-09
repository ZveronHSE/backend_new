package ru.zveron.service

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.BlacklistTest
import ru.zveron.commons.BlacklistServiceEntitiesGenerator.createExistInMultipleBlacklistsRequest
import ru.zveron.commons.BlacklistServiceEntitiesGenerator.createExistsInBlacklistRequest
import ru.zveron.commons.BlacklistServiceEntitiesGenerator.generateBlacklistRecord
import ru.zveron.commons.BlacklistServiceEntitiesGenerator.generateNIds
import ru.zveron.contract.blacklist.deleteAllRecordsWhereUserBlocksRequest
import ru.zveron.contract.blacklist.deleteAllRecordsWhereUserIsBlockedRequest
import ru.zveron.entity.BlacklistRecord
import ru.zveron.repository.BlacklistRepository

@Suppress("BlockingMethodInNonBlockingContext")
class BlacklistServiceInternalTest : BlacklistTest() {


    @Autowired
    lateinit var blacklistRepository: BlacklistRepository

    @Autowired
    lateinit var blacklistService: BlacklistServiceInternal

    @Test
    fun `ExistInBlackList When check if record exists in blacklist and it exists return true`() {
        val (user1Id, user2Id, user3Id) = generateNIds(3)
        runBlocking {
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user1Id, user2Id)))
            val record = blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user1Id, user3Id)))
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user2Id, user3Id)))

            val result = blacklistService.existInBlacklist(createExistsInBlacklistRequest(record))

            result.exists shouldBe true
        }
    }

    @Test
    fun `ExistInBlackList When check if record exists in blacklist and it is not return false`() {
        val (user1Id, user2Id, user3Id, user4Id) = generateNIds(4)
        runBlocking {
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user1Id, user2Id)))
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user1Id, user3Id)))
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user2Id, user3Id)))

            val result = blacklistService.existInBlacklist(createExistsInBlacklistRequest(user1Id, user4Id))

            result.exists shouldBe false
        }
    }

    @Test
    fun `ExistInMultipleBlacklists when some records does not exists`() {
        val (user1Id, user2Id, user3Id, user4Id) = generateNIds(4)
        runBlocking {
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user3Id, user1Id)))
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user2Id, user1Id)))
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user1Id, user4Id)))
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user2Id, user4Id)))
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user3Id, user2Id)))

            val result = blacklistService.existInMultipleBlacklists(
                createExistInMultipleBlacklistsRequest(
                    user1Id,
                    listOf(user1Id, user2Id, user3Id, user4Id)
                )
            )

            result.existsList shouldContainExactly listOf(false, true, true, false)
        }
    }

    @Test
    fun `DeleteAllRecordsWhereUserIsBlocked When delete all records about user every appropriate record should be deleted`() {
        val (user1Id, user2Id, user3Id) = generateNIds(3)
        runBlocking {
            val record1 = blacklistRepository.save(generateBlacklistRecord(user1Id, user2Id))
            val record2 = blacklistRepository.save(generateBlacklistRecord(user3Id, user1Id))
            blacklistRepository.save(generateBlacklistRecord(user1Id, user3Id))
            blacklistRepository.save(generateBlacklistRecord(user2Id, user3Id))

            blacklistService.deleteAllRecordsWhereUserIsBlocked(deleteAllRecordsWhereUserIsBlockedRequest {
                deletedUserId = user3Id
            })

            val set = blacklistRepository.findAll().toSet()
            set.map { it.id }.shouldContainExactlyInAnyOrder(record1.id, record2.id)
        }
    }

    @Test
    fun `DeleteAllRecordsWhereUserBlocks When delete all users records every appropriate record should be deleted`() {
        val (user1Id, user2Id, user3Id) = generateNIds(3)
        runBlocking {
            val record1 = blacklistRepository.save(generateBlacklistRecord(user2Id, user3Id))
            val record2 = blacklistRepository.save(generateBlacklistRecord(user3Id, user1Id))
            blacklistRepository.save(generateBlacklistRecord(user1Id, user2Id))
            blacklistRepository.save(generateBlacklistRecord(user1Id, user3Id))

            blacklistService.deleteAllRecordsWhereUserBlocks(deleteAllRecordsWhereUserBlocksRequest {
                ownerId = user1Id
            })

            val set = blacklistRepository.findAll().toSet()
            set.map { it.id }.shouldContainExactlyInAnyOrder(record1.id, record2.id)
        }
    }
}