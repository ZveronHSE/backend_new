package ru.zveron.apigateway.grpc.service

import com.google.protobuf.DynamicMessage
import com.google.protobuf.util.JsonFormat
import io.grpc.kotlin.ClientCalls
import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.apigateway.grpc.registry.GrpcChannelRegistry
import ru.zveron.apigateway.grpc.registry.MethodDescriptorRegistry
import ru.zveron.apigateway.grpc.registry.ProtoDefinitionRegistry
import ru.zveron.contract.apigateway.ApigatewayRequest
import ru.zveron.contract.apigateway.ApigatewayResponse
import ru.zveron.contract.apigateway.ApigatewayServiceGrpcKt
import ru.zveron.contract.apigateway.apigatewayResponse


@GrpcService
class ApigatewayService(
    private val methodDescriptorRegistry: MethodDescriptorRegistry,
    private val managedChannelRegistry: GrpcChannelRegistry,
    private val protoDefinitionRegistry: ProtoDefinitionRegistry,
) : ApigatewayServiceGrpcKt.ApigatewayServiceCoroutineImplBase() {

    companion object : KLogging()

    override suspend fun receiveServiceCall(request: ApigatewayRequest): ApigatewayResponse {
        //todo: hide under some alias
        val service = request.destination.split(":")[0]
        val method = request.destination.split(":")[1]
        val methodServicePath = request.destination.split(":")[2]
        val protoServiceName = methodServicePath.split(".")[2]

        val channel = managedChannelRegistry.getChannel(service)

        val file = protoDefinitionRegistry.getProtoFileDescriptor(protoServiceName, service)

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
