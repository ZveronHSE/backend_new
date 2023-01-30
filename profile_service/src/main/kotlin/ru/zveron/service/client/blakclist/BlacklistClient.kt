package ru.zveron.service.client.blakclist

interface BlacklistClient {

    suspend fun existsInBlacklist(ownerId: Long, targetUserId: Long): Boolean
}
