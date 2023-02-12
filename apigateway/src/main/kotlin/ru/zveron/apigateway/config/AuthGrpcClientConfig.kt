package ru.zveron.apigateway.config

import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.client.inject.GrpcClientBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.zveron.apigateway.grpc.client.GrpcAuthClient
import ru.zveron.contract.auth.AuthServiceGrpcKt

@Configuration
@GrpcClientBean(
    clazz = AuthServiceGrpcKt.AuthServiceCoroutineStub::class,
    beanName = "authService",
    client = GrpcClient(value = "auth-service")
)
class AuthGrpcClientConfig {

    @Bean
    fun authClient(authService: AuthServiceGrpcKt.AuthServiceCoroutineStub) = GrpcAuthClient(client = authService)
}
