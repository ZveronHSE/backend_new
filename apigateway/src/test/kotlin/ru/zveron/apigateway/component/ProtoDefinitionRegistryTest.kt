package ru.zveron.apigateway.component

import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto
import com.netflix.appinfo.InstanceInfo
import io.grpc.ManagedChannel
import io.grpc.kotlin.ClientCalls
import io.grpc.reflection.v1alpha.FileDescriptorResponse
import io.grpc.reflection.v1alpha.ServerReflectionRequest
import io.grpc.reflection.v1alpha.ServerReflectionResponse
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomUtils
import org.junit.jupiter.api.Test
import org.springframework.cloud.netflix.eureka.EurekaServiceInstance
import org.springframework.cloud.netflix.eureka.reactive.EurekaReactiveDiscoveryClient
import reactor.core.publisher.Flux
import java.util.UUID

class ProtoDefinitionRegistryTest {

    private val channel = mockk<ManagedChannel>()
    private val channelRegistry = mockk<GrpcChannelRegistry>()
    private val eurekaClient = mockk<EurekaReactiveDiscoveryClient>()

    private val protoDefinitionRegistry = ProtoDefinitionRegistry(
        channelRegistry = channelRegistry,
        eurekaClient = eurekaClient,
    )

    @Test
    fun `when no descriptors are present in the map and channel and client respond, then create new descriptor`() =
        runBlocking {
            // given
            val grpcServiceName = "grpc-service-${RandomUtils.nextLong()}"
            val serviceName = "service-name-${RandomUtils.nextLong()}"
            val protoFileName = "${UUID.randomUUID()}.proto"
            val eurekaInstance = EurekaServiceInstance(
                InstanceInfo.Builder.newBuilder().apply {
                    this.setAppName("app-name")
                    this.setMetadata(mapOf(grpcServiceName to protoFileName))
                }.build()
            )
            val methodDescriptor = MethodDescriptorProto.newBuilder().setName("GrpcMethod").setInputType("String").setOutputType("String").build()
            val serviceDescriptor =
                ServiceDescriptorProto.newBuilder().setName("GrpcService").addMethod(methodDescriptor).build()
            val descriptor = FileDescriptorProto.newBuilder().setName(protoFileName).addService(serviceDescriptor).build()
            val reflectionResponse = ServerReflectionResponse.newBuilder().setFileDescriptorResponse(
                FileDescriptorResponse.newBuilder().addFileDescriptorProto(descriptor.toByteString())
            ).build()

            mockkObject(ClientCalls)

            coEvery { eurekaClient.getInstances(serviceName) } returns Flux.just(eurekaInstance)
            coEvery {
                ClientCalls.unaryRpc<ServerReflectionRequest, ServerReflectionResponse>(
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns reflectionResponse
            coEvery { channelRegistry.getChannel(serviceName) } returns channel

            val response = protoDefinitionRegistry.getProtoFileDescriptor(serviceName, grpcServiceName)

            assertSoftly {
                response.name shouldBe descriptor.name
                response.services.size shouldBe 1

                response.services[0].name shouldBe serviceDescriptor.name
                response.services[0].methods.size shouldBe 1

                response.services[0].methods[0].name shouldBe methodDescriptor.name
            }
        }
}
