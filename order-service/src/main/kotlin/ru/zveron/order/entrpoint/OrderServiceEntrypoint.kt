package ru.zveron.order.entrpoint

import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.order.external.GetOrderRequest
import ru.zveron.contract.order.external.GetOrderResponse
import ru.zveron.contract.order.external.OrderServiceExternalGrpcKt
import ru.zveron.contract.order.external.ServiceDeliveryMethod
import ru.zveron.contract.order.external.fullOrder
import ru.zveron.contract.order.external.getOrderResponse
import ru.zveron.contract.order.external.profile
import ru.zveron.contract.order.model.address
import ru.zveron.contract.order.model.animal

@GrpcService
class OrderServiceEntrypoint : OrderServiceExternalGrpcKt.OrderServiceExternalCoroutineImplBase() {

    override suspend fun getOrder(request: GetOrderRequest): GetOrderResponse {
        val profile = profile {
            this.id = 1
            this.name = "name"
            this.imageUrl = "https://storage.yandexcloud.net/zveron-profile/bateman.jpeg"
            this.rating = 4.5f
        }
        val address = address {
            this.town = "Москва"
            this.station = "Площадь Революции"
            this.color = "#0047AB"
        }
        val animal = animal {
            this.id = 1
            this.name = "Ежик"
            this.breed = "Кот"
            this.imageUrl = "https://storage.yandexcloud.net/zveron-animal/cat.jpeg"
        }
        return getOrderResponse {
            this.order = fullOrder {
                this.id = request.id
                this.profile = profile
                this.address = address
                this.animal = animal
                this.serviceDate = "2021-01-01"
                this.description = "Описание"
                this.price = "1000"
                this.serviceDeliveryMethod = ServiceDeliveryMethod.IN_PERSON
                this.title = "Заголовок"
                this.serviceTime = "10:00 - 12:00"
            }
        }
    }
}
