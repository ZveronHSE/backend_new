package ru.zveron.order.entrpoint

import net.devh.boot.grpc.server.service.GrpcService
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import ru.zveron.contract.order.external.*

@GrpcService
class CustomerServiceEntrypoint() : OrderCustomerServiceExternalGrpcKt.OrderCustomerServiceExternalCoroutineImplBase() {

    override suspend fun getCustomer(request: GetCustomerRequest): GetCustomerResponse {
        return getCustomerResponse {
            this.customer = customer {
                this.id = RandomUtils.nextLong()
                this.name = RandomStringUtils.randomAlphanumeric(10)
                this.imageUrl = "https://storage.yandexcloud.net/zveron-profile/random.jpeg"
                this.rating = 4.5f
            }
        }
    }
}