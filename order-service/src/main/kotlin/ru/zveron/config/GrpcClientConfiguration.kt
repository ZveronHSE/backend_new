package ru.zveron.config

import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.client.inject.GrpcClientBean
import net.devh.boot.grpc.client.inject.GrpcClientBeans
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.zveron.contract.address.internal.SubwayStationInternalServiceGrpcKt
import ru.zveron.contract.profile.AnimalServiceInternalGrpcKt
import ru.zveron.contract.profile.ProfileServiceInternalGrpcKt
import ru.zveron.client.address.SubwayGrpcClient
import ru.zveron.client.animal.AnimalGrpcClient
import ru.zveron.client.profile.ProfileGrpcClient

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@GrpcClientBeans(
    GrpcClientBean(
        clazz = SubwayStationInternalServiceGrpcKt.SubwayStationInternalServiceCoroutineStub::class,
        client = GrpcClient("subway")
    ),
    GrpcClientBean(
        clazz = ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub::class,
        client = GrpcClient("profile")
    ),
    GrpcClientBean(
        clazz = AnimalServiceInternalGrpcKt.AnimalServiceInternalCoroutineStub::class,
        client = GrpcClient("animal")
    ),
)
class GrpcClientConfiguration {

    @Bean
    fun profileGrpcClient(
        profileGrpcStub: ProfileServiceInternalGrpcKt.ProfileServiceInternalCoroutineStub,
    ) = ProfileGrpcClient(profileGrpcStub)

    @Bean
    fun animalGrpcClient(
        animalGrpcStub: AnimalServiceInternalGrpcKt.AnimalServiceInternalCoroutineStub,
    ) = AnimalGrpcClient(animalGrpcStub)

    @Bean
    fun subwayGrpcClient(
        subwayGrpcStub: SubwayStationInternalServiceGrpcKt.SubwayStationInternalServiceCoroutineStub,
    ) = ru.zveron.client.address.SubwayGrpcClient(subwayGrpcStub)
}
