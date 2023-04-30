package ru.zveron.client.blacklist

interface BlacklistClient {

    suspend fun existsInBlacklist(ownerId: Long, targetProfileId: Long): Boolean

    suspend fun existsInMultipleBlacklists(targetProfileId: Long, ownersIds: List<Long>): List<Boolean>
}