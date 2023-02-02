package ru.zveron.apigateway.grpc.service

import com.google.protobuf.DynamicMessage
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.kotlin.ClientCalls
import org.springframework.stereotype.Service
import ru.zveron.apigateway.component.AuthResolver
import ru.zveron.apigateway.exception.ApiGatewayException
import ru.zveron.apigateway.grpc.service.dto.GatewayServiceRequest
import ru.zveron.apigateway.persistence.repository.MethodMetadataRepository
import ru.zveron.apigateway.component.GrpcChannelRegistry
import ru.zveron.apigateway.component.ProtoDefinitionRegistry
import ru.zveron.apigateway.component.model.ResolveForRoleRequest
import ru.zveron.apigateway.grpc.client.AccessTokenValid
import ru.zveron.apigateway.grpc.client.GrpcAuthClient
import ru.zveron.apigateway.grpc.context.AuthenticationContext
import ru.zveron.apigateway.persistence.entity.toServiceRole
import ru.zveron.apigateway.utils.DescriptorsUtil.dynamicMessageBuilder
import ru.zveron.apigateway.utils.DescriptorsUtil.getGrpcMethodDescriptor
import ru.zveron.apigateway.utils.DescriptorsUtil.getMethodDescriptor

@Service
class ApiGatewayService(
    private val managedChannelRegistry: GrpcChannelRegistry,
    private val protoDefinitionRegistry: ProtoDefinitionRegistry,
    private val methodMetadataRepository: MethodMetadataRepository,
    private val authResolver: AuthResolver,
) {

    suspend fun handleGatewayCall(request: GatewayServiceRequest): DynamicMessage {
        val metadata = methodMetadataRepository.findByAlias(request.alias)
            ?: throw ApiGatewayException(message = "Non existent method alias", code = Status.Code.INVALID_ARGUMENT)
        val accessToken = AuthenticationContext.current() ?: throw StatusException(Status.DATA_LOSS)

        authResolver.resolveForRole(ResolveForRoleRequest(metadata.accessRole.toServiceRole(), accessToken))

        val channel = managedChannelRegistry.getChannel(metadata.serviceName)

        val protoFileDescriptor =
            protoDefinitionRegistry.getProtoFileDescriptor(metadata.serviceName, metadata.grpcServiceName)

        val protoMethodDescriptor =
            protoFileDescriptor.getMethodDescriptor(metadata.grpcServiceName, metadata.grpcMethodName)

        val grpcMethodDescriptor = protoMethodDescriptor.getGrpcMethodDescriptor()

        val grpcMessage = protoMethodDescriptor.dynamicMessageBuilder(request.requestBody)?.build()

        return ClientCalls.unaryRpc(channel, grpcMethodDescriptor, grpcMessage)
    }
}
