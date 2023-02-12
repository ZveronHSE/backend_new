package ru.zveron.client.favorite

import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.client.inject.GrpcClientBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.zveron.favorites.lot.LotFavoritesServiceGrpcKt

@Configuration
@GrpcClientBean(
    clazz = LotFavoritesServiceGrpcKt.LotFavoritesServiceCoroutineStub::class,
    client = GrpcClient("favorites")
)
class LotFavoriteConfiguration {
    @Bean
    fun lotFavoriteClient(
        lotFavoritesStub: LotFavoritesServiceGrpcKt.LotFavoritesServiceCoroutineStub
    ): LotFavoriteClient {
        return LotFavoriteClient(lotFavoritesStub)
    }
}