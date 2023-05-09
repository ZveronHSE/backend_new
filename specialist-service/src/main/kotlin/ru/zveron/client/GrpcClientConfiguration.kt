package ru.zveron.client

import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.client.inject.GrpcClientBean
import net.devh.boot.grpc.client.inject.GrpcClientBeans
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.zveron.contract.order.internal.OrderServiceInternalGrpcKt

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@GrpcClientBeans(
    GrpcClientBean(
        clazz = OrderServiceInternalGrpcKt.OrderServiceInternalCoroutineStub::class,
        client = GrpcClient("order")
    ),
)
class GrpcClientConfiguration {

    @Bean
    fun profileGrpcClient(
        orderStub: OrderServiceInternalGrpcKt.OrderServiceInternalCoroutineStub
    ) = OrderClient(orderStub)

}
