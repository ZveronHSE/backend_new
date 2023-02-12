package ru.zveron.service.client.lot

import ru.zveron.contract.lot.ProfileLotsResponse

interface LotClient {

    suspend fun getLotsBySellerId(sellerId: Long, userId: Long): ProfileLotsResponse
}