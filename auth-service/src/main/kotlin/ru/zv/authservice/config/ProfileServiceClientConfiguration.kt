package ru.zv.authservice.config

import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.client.inject.GrpcClientBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.zv.authservice.grpc.ProfileServiceClient
import ru.zveron.ProfileServiceInternalGrpcKt

@Configuration
@GrpcClientBean(
    clazz = ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub::class,
    beanName = "profileGrpcClient",
    client = GrpcClient("test"),
)
class ProfileServiceClientConfiguration {

    @Bean
    fun profileGrpcClient(profileGrpcClient: ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub) =
        ProfileServiceClient(profileGrpcClient)
}
