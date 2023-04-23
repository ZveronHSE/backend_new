package ru.zveron.client.lot

import ru.zveron.contract.core.Lot

interface LotClient {

    suspend fun getLotsById(lotIds: List<Long>): List<Lot>
}