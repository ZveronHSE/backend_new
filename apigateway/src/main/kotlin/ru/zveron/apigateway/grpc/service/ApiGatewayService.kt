package ru.zveron.apigateway.grpc.service

import com.google.protobuf.Descriptors
import com.google.protobuf.DynamicMessage
import com.google.protobuf.kotlin.toByteStringUtf8
import com.google.protobuf.util.JsonFormat
import io.grpc.ManagedChannel
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.kotlin.ClientCalls
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import mu.KLogging
import net.logstash.logback.argument.StructuredArguments.keyValue
import net.logstash.logback.argument.StructuredArguments.value
import net.logstash.logback.marker.Markers.aggregate
import net.logstash.logback.marker.Markers.append
import org.springframework.stereotype.Service
import ru.zveron.apigateway.component.AuthResolver
import ru.zveron.apigateway.component.GrpcChannelRegistry
import ru.zveron.apigateway.component.ProtoDefinitionRegistry
import ru.zveron.apigateway.component.model.ResolveForRoleRequest
import ru.zveron.apigateway.exception.ApiGatewayException
import ru.zveron.apigateway.grpc.ApiGatewayMapper.toScope
import ru.zveron.apigateway.grpc.ApiGatewayMapper.toServiceRequest
import ru.zveron.apigateway.grpc.client.ChatGrpcClient
import ru.zveron.apigateway.grpc.context.AuthenticationContext
import ru.zveron.apigateway.grpc.service.dto.GatewayServiceRequest
import ru.zveron.apigateway.persistence.constant.AccessScope
import ru.zveron.apigateway.persistence.entity.MethodMetadata
import ru.zveron.apigateway.persistence.repository.MethodMetadataRepository
import ru.zveron.apigateway.utils.DescriptorsUtil.dynamicMessageBuilder
import ru.zveron.apigateway.utils.DescriptorsUtil.getGrpcMethodDescriptor
import ru.zveron.apigateway.utils.DescriptorsUtil.getMethodDescriptor
import ru.zveron.apigateway.utils.LogstashHelper.toJson
import ru.zveron.contract.apigateway.ApiGatewayRequest
import ru.zveron.contract.apigateway.ApigatewayResponse
import ru.zveron.contract.apigateway.apigatewayResponse
import ru.zveron.contract.chat.ChatRouteRequest

@Service
class ApiGatewayService(
    private val managedChannelRegistry: GrpcChannelRegistry,
    private val protoDefinitionRegistry: ProtoDefinitionRegistry,
    private val methodMetadataRepository: MethodMetadataRepository,
    private val authResolver: AuthResolver,
    private val chatGrpcClient: ChatGrpcClient,
) {

    companion object : KLogging() {
        private val profileIdKey = Metadata.Key.of("profile_id", Metadata.ASCII_STRING_MARSHALLER)
        private val accessTokenKey = Metadata.Key.of("access_token", Metadata.ASCII_STRING_MARSHALLER)
    }

    suspend fun handleGatewayCall(request: GatewayServiceRequest): DynamicMessage {
        logger.debug("Requesting method metadata for {}", value("alias", request.alias))
        val metadata = methodMetadataRepository.findByAlias(request.alias)
            ?: throw ApiGatewayException(message = "Non existent method alias", code = Status.Code.INVALID_ARGUMENT)

        val profileId = verifyUserAccess(metadata.accessScope).also {
            logger.debug(
                "User in context has {}",
                keyValue("profileId", it),
            )
        }

        val accessToken = AuthenticationContext.accessToken()

        logger.debug("Looking for channel for the request for {}", keyValue("serviceName", metadata.serviceName))
        val channel = managedChannelRegistry.getChannel(metadata.serviceName)

        logger.debug(
            "Looking for proto method descriptor for {} {}",
            keyValue("serviceName", metadata.serviceName),
            keyValue("grpcServiceName", metadata.grpcServiceName),
        )
        val protoMethodDescriptor = getProtoMethodDescriptor(metadata)

        val grpcMethodDescriptor = protoMethodDescriptor.getGrpcMethodDescriptor()
        val grpcMessage = protoMethodDescriptor.dynamicMessageBuilder(request.requestBody)?.build()

        logger.debug(
            "Prepared {} to call {}",
            keyValue("message", grpcMessage?.toJson()),
            keyValue("grpcService", metadata.serviceName),
        )
        return tryToCallService(
            channel = channel,
            grpcMethodDescriptor = grpcMethodDescriptor,
            grpcMessage = grpcMessage,
            metadata = Metadata().apply {
                profileId?.let { this.put(profileIdKey, it.toString()) }
                accessToken?.let { this.put(accessTokenKey, accessToken) }
            },
        )
    }

    fun handleBidiStream(requests: Flow<ApiGatewayRequest>): Flow<ApigatewayResponse> = flow {
        val profileId = verifyUserAccess(AccessScope.BUYER).also {
            logger.debug(
                "User in context has {}",
                keyValue("profileId", it),
            )
        }
        val accessToken = AuthenticationContext.accessToken()

        val chatRouteRequests = requests.transform<ApiGatewayRequest, ChatRouteRequest> {
            val requestBuilder = ChatRouteRequest.newBuilder()
            JsonFormat.parser().merge(it.toServiceRequest().requestBody, requestBuilder)
            emit(requestBuilder.build())
        }

        try {
            chatGrpcClient.bidiChatRoute(
                chatRouteRequests,
                Metadata().apply {
                    profileId?.let { this.put(profileIdKey, it.toString()) }
                    accessToken?.let { this.put(accessTokenKey, accessToken) }
                },
            ).collect { message ->
                logger.debug(append("response", message)) { "Stream response" }
                emit(
                    apigatewayResponse {
                        this.responseBody = JsonFormat.printer().print(message).toByteStringUtf8()
                    },
                )
            }
        } catch (ex: StatusException) {
            logger.warn("Response to chat service failed. {}", keyValue("code", ex.status.code))
            throw ex
        } catch (e: Exception) {
            logger.warn("Request failed")
            throw e
        }
    }

    private suspend fun tryToCallService(
        channel: ManagedChannel,
        grpcMethodDescriptor: MethodDescriptor<DynamicMessage?, DynamicMessage>,
        grpcMessage: DynamicMessage?,
        metadata: Metadata,
    ): DynamicMessage = try {
        logger.debug { "Calling service" }
        ClientCalls.unaryRpc(
            channel = channel,
            method = grpcMethodDescriptor,
            request = grpcMessage,
            headers = metadata,
        )
    } catch (ex: StatusException) {
        logger.warn(
            "Response for {} to {} failed. {}",
            keyValue("message", grpcMessage?.toJson()),
            keyValue("method", grpcMethodDescriptor.fullMethodName),
            keyValue("code", ex.status.code),
        )
        throw ex
    } catch (e: Exception) {
        logger.warn(
            aggregate(
                append("method", grpcMethodDescriptor.fullMethodName),
                append("message", grpcMessage?.toJson()),
                append("exception", e.message),
                append("stacktrace", e.stackTrace),
            ),
        ) {
            "Failed request with unknown exception"
        }

        throw e
    }

    private suspend fun verifyUserAccess(accessScope: AccessScope): Long? {
        val accessToken = AuthenticationContext.accessToken()
        logger.debug(
            "Resolving the access for {} and {}",
            keyValue("scope", accessScope.name),
            keyValue("accessToken", accessToken),
        )

        return authResolver.resolveForScope(ResolveForRoleRequest(accessScope.toScope(), accessToken))
    }

    private suspend fun getProtoMethodDescriptor(metadata: MethodMetadata): Descriptors.MethodDescriptor {
        val protoFileDescriptor =
            protoDefinitionRegistry.getProtoFileDescriptor(metadata.serviceName, metadata.grpcServiceName)

        return protoFileDescriptor.getMethodDescriptor(metadata.grpcServiceName, metadata.grpcMethodName)
    }
}
