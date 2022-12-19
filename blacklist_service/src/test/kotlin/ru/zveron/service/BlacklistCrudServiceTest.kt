package ru.zveron.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.AddRequest
import ru.zveron.BlacklistProfile
import ru.zveron.BlacklistTest
import ru.zveron.DeleteAllRecordsWhereUserBlocksRequest
import ru.zveron.DeleteAllRecordsWhereUserIsBlockedRequest
import ru.zveron.DeleteRequest
import ru.zveron.ExistInBlacklistRequest
import ru.zveron.GetListRequest
import ru.zveron.entity.BlacklistRecord
import ru.zveron.exception.BlacklistException
import ru.zveron.repository.BlacklistRepository

class BlacklistCrudServiceTest : BlacklistTest() {

    @Autowired
    lateinit var blacklistRepository: BlacklistRepository

    @Autowired
    lateinit var blacklistCrudService: BlacklistCrudService

    private fun launchBlocking(coroutine: suspend () -> Unit) = runBlocking { launch { coroutine() } }

    @Test
    fun `When check if record exists in blacklist and it exists return true`() {
        launchBlocking {
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(1, 2)))
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(1, 3)))
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(2, 3)))

            val result = blacklistCrudService.existInBlacklist(ExistInBlacklistRequest.newBuilder().setOwnerId(1).setTargetUserId(3).build())

            result.exists shouldBe true
        }
    }

    @Test
    fun `When check if record exists in blacklist and it is not return false`() {
        launchBlocking {
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(1, 2)))
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(1, 3)))
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(2, 3)))

            val result = blacklistCrudService.existInBlacklist(ExistInBlacklistRequest.newBuilder().setOwnerId(1).setTargetUserId(4).build())

            result.exists shouldBe false
        }
    }

    @Test
    fun `When get list of records got records`() {
        launchBlocking {
            val record1 = blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(1, 2)))
            val record2 = blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(1, 3)))
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(2, 3)))

            val set = blacklistCrudService.getList(GetListRequest.newBuilder().setId(1).build()).blacklistRecordsList.toSet()

            set.size shouldBe 2
            set.contains(BlacklistProfile.newBuilder().setId(record1.id.reportedId).build()) shouldBe true
            set.contains(BlacklistProfile.newBuilder().setId(record2.id.reportedId).build()) shouldBe true
        }
    }

    @Test
    fun `When add someone to blacklist new record is created`() {
        launchBlocking {
            blacklistCrudService.add(AddRequest.newBuilder().setOwnerId(1).setTargetUserId(2).build())

            blacklistRepository.findById(BlacklistRecord.BlacklistKey(1, 2)).isPresent shouldBe true
        }
    }

    @Test
    fun `When add someone to blacklist while it is in the black list no exceptions are thrown`() {
        shouldNotThrow<BlacklistException> {
            launchBlocking {
                blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(1, 2)))

                blacklistCrudService.add(AddRequest.newBuilder().setOwnerId(1).setTargetUserId(2).build())
            }
        }
    }

    @Test
    fun `When add myself to blacklist got exception`(){
        val exception = shouldThrow<BlacklistException> {
            launchBlocking {
                blacklistCrudService.add(AddRequest.newBuilder().setOwnerId(1).setTargetUserId(1).build())
            }
        }

        exception.message shouldBe "Нельзя добавить себя в черный список"
    }


    @Test
    fun `When delete someone from blacklist new record is deleted`() {
        launchBlocking {
            val record = blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(1, 2)))

            blacklistCrudService.delete(DeleteRequest.newBuilder().setOwnerId(1).setDeletedUserId(2).build())

            blacklistRepository.findById(record.id).isEmpty shouldBe true
        }
    }

    @Test
    fun `When delete someone from blacklist while it is not in the black list no exceptions are thrown`() {
        shouldNotThrow<BlacklistException> {
            launchBlocking {
                blacklistCrudService.delete(DeleteRequest.newBuilder().setOwnerId(1).setDeletedUserId(2).build())
            }
        }
    }

    @Test
    fun `When delete myself from blacklist got exception`(){
        val exception = shouldThrow<BlacklistException> {
            launchBlocking {
                    blacklistCrudService.delete(DeleteRequest.newBuilder().setOwnerId(1).setDeletedUserId(1).build())
            }
        }

        exception.message shouldBe "Нельзя удалить себя из черного списка"
    }

    @Test
    fun `When delete all records about user every appropriate record should be deleted`() {
        launchBlocking {
            val record1= blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(1, 2)))
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(1, 3)))
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(2, 3)))
            val record2 = blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(3, 1)))

            blacklistCrudService.deleteAllRecordsWhereUserIsBlocked(DeleteAllRecordsWhereUserIsBlockedRequest.newBuilder().setDeletedUserId(3).build())

            val set = blacklistRepository.findAll().toSet()
            set.size shouldBe 2
            set.contains(record1) shouldBe true
            set.contains(record2) shouldBe true
        }
    }

    @Test
    fun `When delete all users records every appropriate record should be deleted`() {
        launchBlocking {
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(1, 2)))
            blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(1, 3)))
            val record1 = blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(2, 3)))
            val record2 = blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(3, 1)))

            blacklistCrudService.deleteAllRecordsWhereUserBlocks(DeleteAllRecordsWhereUserBlocksRequest.newBuilder().setOwnerId(1).build())

            val set = blacklistRepository.findAll().toSet()
            set.size shouldBe 2
            set.contains(record1) shouldBe true
            set.contains(record2) shouldBe true
        }
    }
}