package ru.zveron.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.BlacklistTest
import ru.zveron.commons.BlacklistServiceEntitiesGenerator.createAddToBlacklistRequest
import ru.zveron.commons.BlacklistServiceEntitiesGenerator.createDeleteFromBlacklistRequest
import ru.zveron.commons.BlacklistServiceEntitiesGenerator.createExistsInBlacklistRequest
import ru.zveron.commons.BlacklistServiceEntitiesGenerator.generateBlacklistRecord
import ru.zveron.commons.BlacklistServiceEntitiesGenerator.generateNIds
import ru.zveron.commons.BlacklistServiceEntitiesGenerator.generateUserId
import ru.zveron.deleteAllRecordsWhereUserBlocksRequest
import ru.zveron.deleteAllRecordsWhereUserIsBlockedRequest
import ru.zveron.entity.BlacklistRecord
import ru.zveron.exception.BlacklistException
import ru.zveron.getBlacklistRequest
import ru.zveron.repository.BlacklistRepository

@Suppress("BlockingMethodInNonBlockingContext")
class BlacklistServiceTest : BlacklistTest() {

    @Autowired
    lateinit var blacklistRepository: BlacklistRepository

    @Autowired
    lateinit var blacklistService: BlacklistService

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
    fun `GetBlacklist When get list of records got records`() {
        val (user1Id, user2Id, user3Id) = generateNIds(3)
        runBlocking {
            val record1 = blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user1Id, user2Id)))
            val record2 = blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user1Id, user3Id)))
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user2Id, user3Id)))

            val set = blacklistService.getBlacklist(getBlacklistRequest { id = user1Id }).blacklistUsersList.toSet()

            set.map { it.id }.shouldContainExactlyInAnyOrder(record1.id.reportedUserId, record2.id.reportedUserId)
        }
    }

    @Test
    fun `AddToBlacklist When add someone to blacklist new record is created`() {
        val (user1Id, user2Id) = generateNIds(2)
        runBlocking {
            blacklistService.addToBlacklist(createAddToBlacklistRequest(user1Id, user2Id))

            blacklistRepository.findById(generateBlacklistRecord(user1Id, user2Id).id).isPresent shouldBe true
        }
    }

    @Test
    fun `AddToBlacklist When add someone to blacklist while it is in the black list no exceptions are thrown`() {
        val (user1Id, user2Id) = generateNIds(2)
        shouldNotThrow<BlacklistException> {
            runBlocking {
                blacklistRepository.save(generateBlacklistRecord(user1Id, user2Id))

                blacklistService.addToBlacklist(createAddToBlacklistRequest(user1Id, user2Id))
            }
        }
    }

    @Test
    fun `AddToBlacklist When add myself to blacklist got exception`(){
        val user1Id = generateUserId()
        val exception = shouldThrow<BlacklistException> {
            runBlocking {
                blacklistService.addToBlacklist(createAddToBlacklistRequest(user1Id, user1Id))
            }
        }

        exception.message shouldBe "Нельзя добавить себя в черный список"
    }


    @Test
    fun `DeleteFromBlacklist When delete someone from blacklist new record is deleted`() {
        val (user1Id, user2Id) = generateNIds(2)
        runBlocking {
            val record = blacklistRepository.save(generateBlacklistRecord(user1Id, user2Id))

            blacklistService.deleteFromBlacklist(createDeleteFromBlacklistRequest(user1Id, user2Id))

            blacklistRepository.findById(record.id).isEmpty shouldBe true
        }
    }

    @Test
    fun `DeleteFromBlacklist When delete someone from blacklist while it is not in the black list no exceptions are thrown`() {
        val (user1Id, user2Id) = generateNIds(2)
        shouldNotThrow<BlacklistException> {
            runBlocking {
                blacklistService.deleteFromBlacklist(createDeleteFromBlacklistRequest(user1Id, user2Id))
            }
        }
    }

    @Test
    fun `DeleteFromBlacklist When delete myself from blacklist got exception`(){
        val user1Id = generateUserId()
        val exception = shouldThrow<BlacklistException> {
            runBlocking {
                    blacklistService.deleteFromBlacklist(createDeleteFromBlacklistRequest(user1Id, user1Id))
            }
        }

        exception.message shouldBe "Нельзя удалить себя из черного списка"
    }

    @Test
    fun `DeleteAllRecordsWhereUserIsBlocked When delete all records about user every appropriate record should be deleted`() {
        val (user1Id, user2Id, user3Id) = generateNIds(3)
        runBlocking {
            val record1= blacklistRepository.save(generateBlacklistRecord(user1Id, user2Id))
            val record2 = blacklistRepository.save(generateBlacklistRecord(user3Id, user1Id))
            blacklistRepository.save(generateBlacklistRecord(user1Id, user3Id))
            blacklistRepository.save(generateBlacklistRecord(user2Id, user3Id))

            blacklistService.deleteAllRecordsWhereUserIsBlocked(deleteAllRecordsWhereUserIsBlockedRequest { deletedUserId = user3Id })

            val set = blacklistRepository.findAll().toSet()
            set.map{ it.id }.shouldContainExactlyInAnyOrder(record1.id, record2.id)
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

            blacklistService.deleteAllRecordsWhereUserBlocks(deleteAllRecordsWhereUserBlocksRequest { ownerId = user1Id })

            val set = blacklistRepository.findAll().toSet()
            set.map{ it.id }.shouldContainExactlyInAnyOrder(record1.id, record2.id)
        }
    }
}