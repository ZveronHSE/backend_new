package ru.zveron.authservice.config

import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.client.inject.GrpcClientBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.zveron.authservice.grpc.client.ProfileServiceClient
import ru.zveron.contract.profile.ProfileServiceInternalGrpcKt

@Configuration
@GrpcClientBean(
    clazz = ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub::class,
    beanName = "profileGrpcClient",
    client = GrpcClient("profile-service"),
)
class ProfileServiceClientConfiguration {

    @Bean
    fun profileGrpcServiceClient(
        profileGrpcClient: ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub,
    ): ProfileServiceClient {
        return ProfileServiceClient(profileGrpcClient)
    }
}
