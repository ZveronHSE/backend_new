package ru.zveron.review.config

import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.client.inject.GrpcClientBean
import net.devh.boot.grpc.client.inject.GrpcClientBeans
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.zveron.contract.lot.LotInternalServiceGrpcKt
import ru.zveron.contract.profile.ProfileServiceInternalGrpcKt
import ru.zveron.review.client.profile.ProfileGrpcClient

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@GrpcClientBeans(
    GrpcClientBean(
        clazz = ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub::class,
        client = GrpcClient("profile")
    ),
    GrpcClientBean(
        clazz = LotInternalServiceGrpcKt.LotInternalServiceCoroutineStub::class,
        client = GrpcClient("lot")
    )
)
class GrpcClientConfiguration {

    @Bean
    fun profileGrpcClient(
        profileGrpcStub: ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub,
    ) = ProfileGrpcClient(profileGrpcStub)

    @Bean
    fun lotGrpcClient(
        lotGrpcStub: LotInternalServiceGrpcKt.LotInternalServiceCoroutineStub,
    ) = ru.zveron.review.client.lot.LotGrpcClient(lotGrpcStub)
}
