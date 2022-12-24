package ru.zveron.commons

import org.apache.commons.lang3.RandomUtils
import ru.zveron.addToBlacklistRequest
import ru.zveron.deleteFromBlacklistRequest
import ru.zveron.entity.BlacklistRecord
import ru.zveron.existInBlacklistRequest

object BlacklistServiceEntitiesGenerator {

    fun generateUserId() = RandomUtils.nextLong()

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