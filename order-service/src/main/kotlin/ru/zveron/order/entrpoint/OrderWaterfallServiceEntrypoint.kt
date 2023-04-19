package ru.zveron.order.entrpoint

import net.devh.boot.grpc.server.service.GrpcService
import org.apache.commons.lang3.RandomUtils
import ru.zveron.contract.order.external.GetWaterfallRequest
import ru.zveron.contract.order.external.GetWaterfallResponse
import ru.zveron.contract.order.external.OrderWaterfallServiceExternalGrpcKt
import ru.zveron.contract.order.external.getWaterfallResponse
import ru.zveron.contract.order.external.waterfallOrder
import ru.zveron.contract.order.model.address
import ru.zveron.contract.order.model.animal
import ru.zveron.order.persistence.repository.WaterfallStorage

@GrpcService
class OrderWaterfallServiceEntrypoint(private val waterfallStorage: WaterfallStorage) :
        OrderWaterfallServiceExternalGrpcKt.OrderWaterfallServiceExternalCoroutineImplBase() {

    override suspend fun getWaterfall(request: GetWaterfallRequest): GetWaterfallResponse {
        waterfallStorage.findAll(request.lastOrderId, request.pageSize)
        return getWaterfallResponse {
            this.orders.addAll(
                    List(request.pageSize) { generateWaterfall() }
            )
        }
    }

    private fun generateWaterfall(id: Long = RandomUtils.nextLong()) = waterfallOrder {
        this.id = id
        this.address = address {
            this.town = "Москва"
            this.station = "Площадь Революции"
            this.color = "#0047AB"
        }
        this.serviceDate = "2021-01-01"
        this.animal = animal {
            this.id = 1
            this.name = "Ежик"
            this.breed = "Кот"
            this.imageUrl = "https://storage.yandexcloud.net/zveron-animal/cat.jpeg"
        }
        this.price = RandomUtils.nextLong(100, 10_000).toString()
        this.title = "Заголовок"
    }
}
