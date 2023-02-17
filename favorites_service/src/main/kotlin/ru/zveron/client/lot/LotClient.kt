package ru.zveron.client.lot

import ru.zveron.contract.lot.LotsIdResponse

interface LotClient {

    suspend fun getLotsById(lotIds: List<Long>): LotsIdResponse
}