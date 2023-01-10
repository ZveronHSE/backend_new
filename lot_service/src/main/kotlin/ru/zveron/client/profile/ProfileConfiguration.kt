package ru.zveron.client.profile

import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.client.inject.GrpcClientBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.zveron.contract.profile.ProfileServiceInternalGrpcKt

@Configuration
@GrpcClientBean(
    clazz = ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub::class,
    client = GrpcClient("profile")
)
class ProfileConfiguration {
    @Bean
    fun profileClient(
        profileStub: ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub
    ): ProfileClient {
        return ProfileClient(profileStub)
    }
}