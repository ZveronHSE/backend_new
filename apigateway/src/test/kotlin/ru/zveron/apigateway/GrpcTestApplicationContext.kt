package ru.zveron.apigateway

import com.ninjasquad.springmockk.MockkBean
import io.grpc.ClientInterceptor
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import io.mockk.mockk
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import ru.zveron.apigateway.component.GrpcChannelRegistry
import ru.zveron.apigateway.component.ProtoDefinitionRegistry
import ru.zveron.apigateway.grpc.client.GrpcAuthClient
import ru.zveron.contract.auth.AuthServiceGrpcKt
import java.util.UUID

@SpringBootApplication
class GrpcTestApplicationContext {

    @Bean
    fun headerAddingInterceptor(): ClientInterceptor = MetadataUtils.newAttachHeadersInterceptor(Metadata().apply {
        put(Metadata.Key.of("access_token", Metadata.ASCII_STRING_MARSHALLER), "access_token.${UUID.randomUUID()}")
    })

    @Bean
    @Primary
    fun authClient(): GrpcAuthClient {
        val clientMock = mockk<AuthServiceGrpcKt.AuthServiceCoroutineStub>(relaxed = true)
        return GrpcAuthClient(clientMock)
    }

    @MockkBean(name = "protoDefinitionRegistry")
    private lateinit var protoDefinitionRegistry: ProtoDefinitionRegistry

    @MockkBean(name = "grpcChannelRegistry")
    private lateinit var grpcChannelRegistry: GrpcChannelRegistry
}
