package ru.zveron.repository

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.BlacklistTest
import ru.zveron.commons.BlacklistServiceEntitiesGenerator
import ru.zveron.entity.BlacklistRecord

class RepositoryTest : BlacklistTest() {

    @Autowired
    lateinit var blacklistRepository: BlacklistRepository

    @Test
    fun `GetAllOwnersIdsIfRecordExists when some records does not exists`() {
        val (user1Id, user2Id, user3Id, user4Id) = BlacklistServiceEntitiesGenerator.generateNIds(4)

        blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user3Id, user1Id)))
        blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user2Id, user1Id)))
        blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user1Id, user4Id)))
        blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user2Id, user4Id)))
        blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(user3Id, user2Id)))

        blacklistRepository.getAllOwnersIdsIfRecordExists(
            user1Id,
            listOf(user2Id, user3Id, user4Id)
        ) shouldContainExactlyInAnyOrder listOf(user3Id, user2Id)
    }
}