package ru.zveron.service.api.lot

import ru.zveron.contract.lot.ProfileLotsResponse

interface LotService {

    suspend fun getLotsBySellerId(profileId: Long): ProfileLotsResponse
}