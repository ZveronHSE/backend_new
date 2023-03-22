package ru.zveron.client.blacklist

interface BlacklistClient {

    suspend fun existsInBlacklist(ownerId: Long, targetUserId: Long): Boolean
}