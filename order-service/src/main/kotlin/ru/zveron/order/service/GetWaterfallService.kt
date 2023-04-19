package ru.zveron.order.service

import org.springframework.stereotype.Service
import ru.zveron.order.persistence.repository.WaterfallStorage
import ru.zveron.order.service.dto.GetWaterfallRequest


@Service
class GetWaterfallService(
        private val waterfallStorage: WaterfallStorage,
) {

    suspend fun getWaterfall(request: GetWaterfallRequest) {
        return waterfallStorage.findAll(request.lastOrderId ?: 0, request.pageSize)
    }
}