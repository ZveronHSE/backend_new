package ru.zveron.apigateway.config

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.mockk
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import ru.zveron.apigateway.component.GrpcChannelRegistry
import ru.zveron.apigateway.component.ProtoDefinitionRegistry
import ru.zveron.apigateway.grpc.client.GrpcAuthClient
import ru.zveron.contract.auth.AuthServiceGrpcKt
import ru.zveron.contract.auth.profileId

@Configuration
@ImportAutoConfiguration(
    GrpcServerAutoConfiguration::class,
    GrpcServerFactoryAutoConfiguration::class,
    GrpcClientAutoConfiguration::class
)
class GrpcServiceTestConfig {

    @Bean
    @Primary
    fun authClient(): GrpcAuthClient {
        val clientMock = mockk<AuthServiceGrpcKt.AuthServiceCoroutineStub>()
        coEvery { clientMock.verifyToken(any(), any()) } returns profileId { this.id = 123 }
        return GrpcAuthClient(clientMock)
    }

    @MockkBean(name = "protoDefinitionRegistry")
    private lateinit var protoDefinitionRegistry: ProtoDefinitionRegistry

    @MockkBean(name = "grpcChannelRegistry")
    private lateinit var grpcChannelRegistry: GrpcChannelRegistry
}
