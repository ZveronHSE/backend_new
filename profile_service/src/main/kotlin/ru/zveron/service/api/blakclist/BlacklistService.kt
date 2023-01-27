package ru.zveron.service.api.blakclist

interface BlacklistService {

    suspend fun existsInBlacklist(ownerId: Long, targetUserId: Long): Boolean
}