package ru.zveron.client.profile

import ru.zveron.contract.profile.ProfileSummary


interface ProfileClient {

    suspend fun getProfilesSummary(ids: List<Long>): List<ProfileSummary>

    suspend fun existsById(id: Long): Boolean
}