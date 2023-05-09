package ru.zveron.client

import io.grpc.Status
import io.grpc.StatusException
import ru.zveron.contract.order.internal.FullOrder
import ru.zveron.contract.order.internal.OrderServiceInternalGrpcKt
import ru.zveron.contract.order.internal.ServiceDeliveryMethod
import ru.zveron.contract.order.internal.fullOrder
import ru.zveron.expection.SpecialistException

class OrderClient(
    private val orderStub: OrderServiceInternalGrpcKt.OrderServiceInternalCoroutineStub
) {

    suspend fun getOrderByID(id: Long): FullOrder {
        return try {
            // TODO order done endpoint
//            val response = orderStub.getOrder(
//                int64Value {
//                    value = id
//                }
//            )


            fullOrder {
                this.id = id
                title = "груминг"
                price = ""
                // TODO address
                serviceDeliveryMethod = ServiceDeliveryMethod.DEPARTURE
            }
        } catch (e: StatusException) {
            throw SpecialistException(e.message!!, Status.INTERNAL)
        }
    }
}