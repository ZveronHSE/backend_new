package ru.zveron.client.profile

import ru.zveron.contract.profile.GetProfilesSummaryResponse

interface ProfileClient {

    suspend fun getProfilesSummary(ids: List<Long>): GetProfilesSummaryResponse
}