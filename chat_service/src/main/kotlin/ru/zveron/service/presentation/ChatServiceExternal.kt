package ru.zveron.service.presentation

import com.datastax.oss.driver.api.core.uuid.Uuids
import io.grpc.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transform
import mu.KLogging
import net.devh.boot.grpc.server.service.GrpcService
import net.logstash.logback.argument.StructuredArguments.keyValue
import net.logstash.logback.marker.Markers
import ru.zveron.contract.chat.ChatRouteRequest
import ru.zveron.contract.chat.ChatRouteResponse
import ru.zveron.contract.chat.ChatServiceExternalGrpcKt
import ru.zveron.exception.ChatException
import ru.zveron.exception.InvalidParamChatException
import ru.zveron.library.grpc.util.GrpcUtils
import ru.zveron.model.dao.ChatRequestContext
import ru.zveron.model.dao.ChatRouteResponseWrapper
import ru.zveron.model.dao.MultipleConnectionsResponse
import ru.zveron.model.dao.NoneConnectionResponse
import ru.zveron.model.dao.SingleConnectionResponse
import ru.zveron.service.application.ChatApplicationService
import ru.zveron.component.ChatPersistence
import ru.zveron.contract.chat.chatRouteResponse
import ru.zveron.contract.chat.errorMessage
import ru.zveron.service.application.MessageApplicationService
import java.util.UUID
import kotlin.coroutines.coroutineContext

@GrpcService
class ChatServiceExternal(
    private val chatApplicationService: ChatApplicationService,
    private val chatPersistence: ChatPersistence,
    private val messageApplicationService: MessageApplicationService,
) : ChatServiceExternalGrpcKt.ChatServiceExternalCoroutineImplBase() {

    companion object : KLogging()

    private val fixedNodeAddress = UUID.fromString("3e199891-cff7-11ed-bd31-479ce559ae0d")

    override fun bidiChatRoute(requests: Flow<ChatRouteRequest>): Flow<ChatRouteResponse> {
        val connectionId = Uuids.timeBased()

        return flow {
            logger.info("Start connection $connectionId")
            val chatRequestContext = ChatRequestContext(
                connectionId,
                GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!
            )
            chatPersistence.registerConnection(fixedNodeAddress, chatRequestContext)

            val requestFlow = requests
                .transform {
                    logger.info(
                        "Process request with type ${it.requestCase.name} {}",
                        keyValue("connection-id", connectionId)
                    )

                    try {
                        handleRequest(it, chatRequestContext)
                    } catch (ex: Exception) {
                        suppressChatException(ex)
                    }
                }
                .onCompletion {
                    logger.info("Close {}", keyValue("connection-id", connectionId))
                    chatPersistence.closeConnection(chatRequestContext.authorizedProfileId, chatRequestContext)
                }
            val responseFlow =
                chatPersistence.getChannel(chatRequestContext.authorizedProfileId)?.receiveAsFlow()
                    ?: throw ChatException(
                        Status.INTERNAL,
                        "Connection for user with id: ${chatRequestContext.authorizedProfileId} not found",
                        chatRequestContext,
                    )

            merge(requestFlow, responseFlow).collect { emit(it) }
        }
    }

    private suspend fun handleRequest(
        request: ChatRouteRequest,
        context: ChatRequestContext,
    ) {
        val response = when (request.requestCase) {
            ChatRouteRequest.RequestCase.GET_RECENT_CHATS -> chatApplicationService.getRecentChats(
                request.getRecentChats,
                context
            )

            ChatRouteRequest.RequestCase.GET_CHAT_RECENT_MESSAGES -> messageApplicationService.getRecentMessagesByChat(
                request.getChatRecentMessages,
                context,
            )

            ChatRouteRequest.RequestCase.GET_CHAT -> chatApplicationService.getChatSummary(
                request.getChat,
                context,
            )

            ChatRouteRequest.RequestCase.SEND_MESSAGE -> messageApplicationService.sendMessage(
                request.sendMessage,
                context
            )

            ChatRouteRequest.RequestCase.SEND_EVENT -> chatApplicationService.sendEvent(
                request.sendEvent,
                context,
            )

            ChatRouteRequest.RequestCase.ATTACH_LOT -> chatApplicationService.attachLotToChat(
                request.attachLot,
                context
            )

            ChatRouteRequest.RequestCase.DETACH_LOT -> chatApplicationService.detachLotFromChat(
                request.detachLot,
                context,
            )

            ChatRouteRequest.RequestCase.START_CHAT -> chatApplicationService.startChat(
                request.startChat,
                context
            )

            else -> throw InvalidParamChatException("'request' property is not set", context)
        }

        deliverResponse(response, context)
    }

    private suspend fun deliverResponse(response: ChatRouteResponseWrapper, context: ChatRequestContext) {
        when (response) {
            is SingleConnectionResponse -> chatPersistence.sendMessageToConnection(
                response.targetProfileId,
                response.responseBody,
                context,
            )

            is MultipleConnectionsResponse -> response.responses.forEach { (profileId, responseBody) ->
                chatPersistence.sendMessageToConnection(
                    profileId,
                    responseBody,
                    context,
                )
            }

            is NoneConnectionResponse -> {}
        }
    }

    private suspend fun FlowCollector<ChatRouteResponse>.suppressChatException(ex: Throwable) {
        if (ex !is ChatException) {
            throw ex
        }

        val marker = Markers.append("connection-id", ex.context.connectionId)
        logger.debug(
            marker,
            "Suppress chat exception with status ${ex.status} and send error message connection-id=${ex.context.connectionId}",
            ex
        )
        emit(chatRouteResponse {
            error = errorMessage {
                status = ex.status.code.value()
                message = ex.message!!
            }
        })
    }
}