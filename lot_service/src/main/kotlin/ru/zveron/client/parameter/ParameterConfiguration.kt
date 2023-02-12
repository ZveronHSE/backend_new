package ru.zveron.client.parameter

import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.client.inject.GrpcClientBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.zveron.contract.category.CategoryServiceGrpcKt
import ru.zveron.contract.parameter.ParameterServiceGrpcKt

@Configuration
@GrpcClientBean(
    clazz = ParameterServiceGrpcKt.ParameterServiceCoroutineStub::class,
    client = GrpcClient("parameter")
)
@GrpcClientBean(
    clazz = CategoryServiceGrpcKt.CategoryServiceCoroutineStub::class,
    client = GrpcClient("parameter")
)
class ParameterConfiguration {
    @Bean
    fun parameterClient(
        parameterStub: ParameterServiceGrpcKt.ParameterServiceCoroutineStub,
        categoryStub: CategoryServiceGrpcKt.CategoryServiceCoroutineStub
    ): ParameterClient {
        return ParameterClient(parameterStub, categoryStub)
    }
}