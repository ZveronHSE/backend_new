package ru.zveron.service.client.lot

import ru.zveron.contract.lot.ProfileLotsResponse

interface LotClient {

    suspend fun getLotsBySellerId(profileId: Long): ProfileLotsResponse
}