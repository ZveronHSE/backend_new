package ru.zveron.commons

import org.apache.commons.lang3.RandomUtils
import ru.zveron.contract.blacklist.addToBlacklistRequest
import ru.zveron.contract.blacklist.deleteFromBlacklistRequest
import ru.zveron.contract.blacklist.existInBlacklistRequest
import ru.zveron.contract.profile.profileSummary
import ru.zveron.entity.BlacklistRecord

object BlacklistServiceEntitiesGenerator {

    fun generateUserId() = RandomUtils.nextLong()

    fun generateNIds(n: Int) = List(n) { generateUserId() }

    fun generateString(n: Int) = String(CharArray(n) {
        RandomUtils.nextInt('a'.code, 'z'.code + 1).toChar()
            .let { if (RandomUtils.nextBoolean()) it.uppercaseChar() else it }
    })

    fun generateBlacklistRecord(ownerUserId: Long, reportedUserId: Long) =
        BlacklistRecord(BlacklistRecord.BlacklistKey(ownerUserId = ownerUserId, reportedUserId = reportedUserId))

    fun createExistsInBlacklistRequest(record: BlacklistRecord) =
        createExistsInBlacklistRequest(record.id.ownerUserId, record.id.reportedUserId)

    fun createExistsInBlacklistRequest(ownerId: Long, targetUserId: Long) =
        existInBlacklistRequest {
            this.ownerId = ownerId
            this.targetUserId = targetUserId
        }

    fun createAddToBlacklistRequest(targetUserId: Long) =
        addToBlacklistRequest { this.id = targetUserId }

    fun createDeleteFromBlacklistRequest(deletedUserId: Long) =
        deleteFromBlacklistRequest { this.id = deletedUserId }

    fun generateProfileSummary(id: Long) = profileSummary {
        this.id = id
        name = generateString(10)
        surname = generateString(10)
        imageId = generateUserId()
        addressId = generateUserId()
    }
}