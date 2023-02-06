package ru.zveron.apigateway.e2e

import com.google.protobuf.Descriptors
import com.google.protobuf.kotlin.toByteStringUtf8
import com.google.protobuf.util.JsonFormat
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import kotlinx.coroutines.runBlocking
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import ru.zveron.apigateway.ApigatewayApplication
import ru.zveron.apigateway.component.GrpcChannelRegistry
import ru.zveron.apigateway.component.ProtoDefinitionRegistry
import ru.zveron.apigateway.config.ContainerConfigurer
import ru.zveron.apigateway.config.GrpcServiceTestConfig
import ru.zveron.contract.apigateway.ApigatewayServiceGrpcKt
import ru.zveron.contract.apigateway.apiGatewayRequest
import ru.zveron.contract.auth.TestRequest
import ru.zveron.contract.auth.testRequest
import ru.zveron.contract.auth.testResponse
import java.util.UUID

@SpringBootTest(
    properties = [
        "grpc.server.in-process-name=test",
        "grpc.client.apigw-client.address=in-process:test",
        "grpc.client.apigw-client-no-header.address=in-process:test",
        "grpc.client.auth-service.address=in-process:test",
        "spring.main.allow-bean-definition-overriding=true",
        "logging.level.ru.zveron.apigateway=DEBUG",
        "grpc.server.port=0",
    ],
    classes = [ApigatewayApplication::class, AuthServiceDummyImpl::class]
)
@SpringJUnitConfig(GrpcServiceTestConfig::class)
@DirtiesContext
class ApiGatewaySmokeTest : ContainerConfigurer() {

    @Test
    fun contextLoads() {
    }

    @GrpcClient(value = "apigw-client")
    private lateinit var grpc: ApigatewayServiceGrpcKt.ApigatewayServiceCoroutineStub

    @GrpcClient("apigw-client-no-header")
    private lateinit var grpcNoHeader: ApigatewayServiceGrpcKt.ApigatewayServiceCoroutineStub

    @Autowired
    private lateinit var protoDefinitionRegistry: ProtoDefinitionRegistry

    @Autowired
    private lateinit var grpcChannelRegistry: GrpcChannelRegistry

    @Test
    fun `verify context populated with metadata`(): Unit = runBlocking {
        coEvery { grpcChannelRegistry.getChannel(any()) } returns ManagedChannelBuilder.forTarget(
            "self:///auth-service"
        ).usePlaintext()
            .build()

        val jsonRequest = testRequest {
            this.name = "test request"
            this.isTrue = true
        }.let { JsonFormat.printer().print(it) }

        coEvery {
            protoDefinitionRegistry.getProtoFileDescriptor(
                any(),
                any(),
            )
        } returns Descriptors.FileDescriptor.buildFrom(TestRequest.getDescriptor().file.toProto(), emptyArray())

        val md = Metadata().apply {
            put(Metadata.Key.of("access_token", Metadata.ASCII_STRING_MARSHALLER), "access_token.${UUID.randomUUID()}")
        }
        val response = grpc.callApiGateway(apiGatewayRequest {
            this.methodAlias = "testBuyer"
            this.requestBody = jsonRequest.toByteStringUtf8()
        }, md)

        response.responseBody.toStringUtf8() shouldBe JsonFormat.printer()
            .print(testResponse { this.response = "any response" })
    }

    @Test
    fun `verify when no token in metadata then throws exception`(): Unit = runBlocking {
        val jsonRequest = testRequest {
            this.name = "test request"
            this.isTrue = true
        }.let { JsonFormat.printer().print(it) }

        val status = shouldThrow<StatusException> {
            grpcNoHeader.callApiGateway(apiGatewayRequest {
                this.methodAlias = "testBuyer"
                this.requestBody = jsonRequest.toByteStringUtf8()
            })
        }.status

        status shouldBe Status.DATA_LOSS
    }

    @Test
    fun `verify when no access token can still access methods for ANY`() {
        coEvery { grpcChannelRegistry.getChannel(any()) } returns ManagedChannelBuilder.forTarget(
            "self:///auth-service"
        ).usePlaintext()
            .build()

        val jsonRequest = testRequest {
            this.name = "test request"
            this.isTrue = true
        }.let { JsonFormat.printer().print(it) }

        coEvery {
            protoDefinitionRegistry.getProtoFileDescriptor(
                any(),
                any()
            )
        } returns Descriptors.FileDescriptor.buildFrom(TestRequest.getDescriptor().file.toProto(), emptyArray())

        val response = runBlocking {
            grpcNoHeader.callApiGateway(apiGatewayRequest {
                this.methodAlias = "testAny"
                this.requestBody = jsonRequest.toByteStringUtf8()
            })
        }

        response.responseBody.toStringUtf8() shouldBe JsonFormat.printer()
            .print(testResponse { this.response = "buyer response" })
    }
}