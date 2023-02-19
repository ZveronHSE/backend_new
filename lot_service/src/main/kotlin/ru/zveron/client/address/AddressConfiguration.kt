package ru.zveron.client.address

import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.client.inject.GrpcClientBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.zveron.contract.address.AddressServiceGrpcKt

@Configuration
@GrpcClientBean(
    clazz = AddressServiceGrpcKt.AddressServiceCoroutineStub::class,
    client = GrpcClient("address")
)
class AddressConfiguration {
    @Bean
    fun addressClient(
        addressStub: AddressServiceGrpcKt.AddressServiceCoroutineStub
    ): AddressClient {
        return AddressClient(addressStub)
    }
}