package ru.zveron.apigateway.grpc.service

import com.google.protobuf.DynamicMessage
import com.google.protobuf.util.JsonFormat
import io.grpc.kotlin.ClientCalls
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.cloud.netflix.eureka.reactive.EurekaReactiveDiscoveryClient
import ru.zveron.apigateway.grpc.registry.GrpcChannelRegistry
import ru.zveron.apigateway.grpc.registry.MethodDescriptorRegistry
import ru.zveron.apigateway.grpc.registry.ProtoDefinitionRegistry
import ru.zveron.contract.apigateway.ApiGatewayRequest
import ru.zveron.contract.apigateway.ApigatewayResponse
import ru.zveron.contract.apigateway.ApigatewayServiceGrpcKt
import ru.zveron.contract.apigateway.apigatewayResponse


@GrpcService
class ApigatewayService(
    private val methodDescriptorRegistry: MethodDescriptorRegistry,
    private val managedChannelRegistry: GrpcChannelRegistry,
    private val protoDefinitionRegistry: ProtoDefinitionRegistry,
    private val client: EurekaReactiveDiscoveryClient,
) : ApigatewayServiceGrpcKt.ApigatewayServiceCoroutineImplBase() {

    companion object : KLogging()

    override suspend fun callApiGateway(request: ApiGatewayRequest): ApigatewayResponse {
        //todo: hide under some alias
        val service = request.methodAlias.split(":")[0]
        val method = request.methodAlias.split(":")[1]
        val methodServicePath = request.methodAlias.split(":")[2]
        val protoServiceName = methodServicePath.split(".")[2]

        val channel = managedChannelRegistry.getChannel(service)

        val file = try {
            protoDefinitionRegistry.getProtoFileDescriptor(protoServiceName, service)

        } catch (ex: Exception) {
            logger.error { ex }
            throw ex
        }

        //todo: extension
        val protoMethodDescriptor = file.services.find { it.name.equals(protoServiceName, true) }
            ?.methods
            ?.find {
                it.name.equals(method, true)
            } ?: error("No such method is found")

        val requestMethodDescriptor =
            methodDescriptorRegistry.resolveDescriptor(methodServicePath, method, protoMethodDescriptor)

        val dynMessage = DynamicMessage.newBuilder(protoMethodDescriptor.inputType)
        JsonFormat.parser().merge(request.requestBody.toStringUtf8(), dynMessage)

        val response = ClientCalls.unaryRpc(
            channel,
            requestMethodDescriptor,
            dynMessage.build()
        )

        return apigatewayResponse {
            this.responseBody = response.toByteString()
        }
    }
}
