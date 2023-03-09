package ru.zveron.apigateway.grpc.service

import com.google.protobuf.Descriptors
import com.google.protobuf.DynamicMessage
import io.grpc.ManagedChannel
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.Status
import io.grpc.kotlin.ClientCalls
import mu.KLogging
import net.logstash.logback.marker.Markers.append
import org.springframework.stereotype.Service
import ru.zveron.apigateway.component.AuthResolver
import ru.zveron.apigateway.component.GrpcChannelRegistry
import ru.zveron.apigateway.component.ProtoDefinitionRegistry
import ru.zveron.apigateway.component.model.ResolveForRoleRequest
import ru.zveron.apigateway.exception.ApiGatewayException
import ru.zveron.apigateway.grpc.ApiGatewayMapper.toScope
import ru.zveron.apigateway.grpc.context.AuthenticationContext
import ru.zveron.apigateway.grpc.service.dto.GatewayServiceRequest
import ru.zveron.apigateway.persistence.constant.AccessScope
import ru.zveron.apigateway.persistence.entity.MethodMetadata
import ru.zveron.apigateway.persistence.repository.MethodMetadataRepository
import ru.zveron.apigateway.utils.DescriptorsUtil.dynamicMessageBuilder
import ru.zveron.apigateway.utils.DescriptorsUtil.getGrpcMethodDescriptor
import ru.zveron.apigateway.utils.DescriptorsUtil.getMethodDescriptor
import ru.zveron.apigateway.utils.LogstashHelper.toJson

@Service
class ApiGatewayService(
    private val managedChannelRegistry: GrpcChannelRegistry,
    private val protoDefinitionRegistry: ProtoDefinitionRegistry,
    private val methodMetadataRepository: MethodMetadataRepository,
    private val authResolver: AuthResolver,
) {

    companion object : KLogging() {
        private val profileIdKey = Metadata.Key.of("profile_id", Metadata.ASCII_STRING_MARSHALLER)
        private val accessTokenKey = Metadata.Key.of("access_token", Metadata.ASCII_STRING_MARSHALLER)
    }

    suspend fun handleGatewayCall(request: GatewayServiceRequest): DynamicMessage {
        logger.debug(append("requestAlias", request.alias)) { "Handling gateway call request" }

        val metadata = methodMetadataRepository.findByAlias(request.alias)
            ?: throw ApiGatewayException(message = "Non existent method alias", code = Status.Code.INVALID_ARGUMENT)

        val profileId = verifyUserAccess(metadata.accessScope)
        val accessToken = AuthenticationContext.accessToken()
        val channel = managedChannelRegistry.getChannel(metadata.serviceName)
        val protoMethodDescriptor = getProtoMethodDescriptor(metadata)
        val grpcMethodDescriptor = protoMethodDescriptor.getGrpcMethodDescriptor()
        val grpcMessage = protoMethodDescriptor.dynamicMessageBuilder(request.requestBody)?.build()

        logger.debug(
            append("grpcRequest", grpcMessage?.allFields?.toJson()).and(append("grpcService", metadata.serviceName)),
            "Calling grpc service"
        )

        return tryToCallService(
            channel = channel,
            grpcMethodDescriptor = grpcMethodDescriptor,
            grpcMessage = grpcMessage,
            metadata = Metadata().apply {
                profileId?.let { this.put(profileIdKey, it.toString()) }
                accessToken?.let { this.put(accessTokenKey, accessToken) }
            }
        )
    }

    private suspend fun tryToCallService(
        channel: ManagedChannel,
        grpcMethodDescriptor: MethodDescriptor<DynamicMessage?, DynamicMessage>,
        grpcMessage: DynamicMessage?,
        metadata: Metadata,
    ): DynamicMessage = try {
        ClientCalls.unaryRpc(
            channel = channel,
            method = grpcMethodDescriptor,
            request = grpcMessage,
            headers = metadata,
        )
    } catch (e: Exception) {
        logger.error(e) { "Failed service request $e" }
        throw e
    }

    private suspend fun verifyUserAccess(accessScope: AccessScope): Long? {
        val accessToken = AuthenticationContext.accessToken()
        logger.debug(append("accessToken", accessToken), "Access token in the context")

        return authResolver.resolveForScope(ResolveForRoleRequest(accessScope.toScope(), accessToken))
    }

    private suspend fun getProtoMethodDescriptor(metadata: MethodMetadata): Descriptors.MethodDescriptor {
        val protoFileDescriptor =
            protoDefinitionRegistry.getProtoFileDescriptor(metadata.serviceName, metadata.grpcServiceName)

        return protoFileDescriptor.getMethodDescriptor(metadata.grpcServiceName, metadata.grpcMethodName)
    }
}
