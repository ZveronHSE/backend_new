package ru.zveron.apigateway.grpc.service

import com.google.protobuf.DynamicMessage
import io.grpc.kotlin.ClientCalls
import mu.KLogging
import org.springframework.stereotype.Service
import ru.zveron.apigateway.grpc.controller.ApiGatewayGrpcController
import ru.zveron.apigateway.registry.GrpcChannelRegistry
import ru.zveron.apigateway.registry.ProtoDefinitionRegistry
import ru.zveron.apigateway.utils.DescriptorsUtil.dynamicMessageBuilder
import ru.zveron.apigateway.utils.DescriptorsUtil.getGrpcMethodDescriptor
import ru.zveron.apigateway.utils.DescriptorsUtil.getMethodDescriptor

@Service
class ApiGatewayService(
    private val managedChannelRegistry: GrpcChannelRegistry,
    private val protoDefinitionRegistry: ProtoDefinitionRegistry,
) {

    companion object : KLogging()

    suspend fun handleGatewayCall(
        service: String,
        grpcService: String,
        grpcMethod: String,
        request: String,
    ): DynamicMessage {

        val channel = managedChannelRegistry.getChannel(service)

        val file = try {
            protoDefinitionRegistry.getProtoFileDescriptor(service, grpcService)
        } catch (ex: Exception) {
            ApiGatewayGrpcController.logger.error { ex }
            throw ex
        }

        val protoMethodDescriptor = file.getMethodDescriptor(grpcService, grpcMethod)

        val grpcMethodDescriptor = protoMethodDescriptor.getGrpcMethodDescriptor()

        val grpcMessageBuilder = protoMethodDescriptor.dynamicMessageBuilder(request)

        return ClientCalls.unaryRpc(
            channel,
            grpcMethodDescriptor,
            grpcMessageBuilder?.build()
        )
    }
}