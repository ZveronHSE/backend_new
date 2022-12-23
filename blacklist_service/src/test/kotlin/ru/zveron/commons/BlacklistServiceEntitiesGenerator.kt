package ru.zveron.commons

import ru.zveron.addToBlacklistRequest
import ru.zveron.deleteFromBlacklistRequest
import ru.zveron.entity.BlacklistRecord
import ru.zveron.existInBlacklistRequest
import kotlin.random.Random

object BlacklistServiceEntitiesGenerator {

    fun generateUserId() = Random.nextLong(1, Long.MAX_VALUE)

    fun generateNIds(n: Int) = List(n) { generateUserId() }

    fun generateBlacklistRecord(ownerUserId: Long, reportedUserId: Long) =
        BlacklistRecord(BlacklistRecord.BlacklistKey(ownerUserId = ownerUserId, reportedUserId = reportedUserId))

    fun createExistsInBlacklistRequest(record: BlacklistRecord) =
        createExistsInBlacklistRequest(record.id.ownerUserId, record.id.reportedUserId)

    fun createExistsInBlacklistRequest(ownerId: Long, targetUserId: Long) =
        existInBlacklistRequest {
            this.ownerId = ownerId
            this.targetUserId = targetUserId
        }

    fun createAddToBlacklistRequest(ownerId: Long, targetUserId: Long) =
        addToBlacklistRequest {
            this.ownerId = ownerId
            this.targetUserId = targetUserId
        }

    fun createDeleteFromBlacklistRequest(ownerId: Long, deletedUserId: Long) =
        deleteFromBlacklistRequest {
            this.ownerId = ownerId
            this.deletedUserId = deletedUserId
        }
}