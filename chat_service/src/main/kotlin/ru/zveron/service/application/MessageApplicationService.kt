package ru.zveron.service.application

import io.grpc.Status
import kotlinx.coroutines.flow.toList
import mu.KLogging
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.springframework.stereotype.Service
import ru.zveron.client.blacklist.BlacklistClient
import ru.zveron.client.snowflake.SnowflakeClient
import ru.zveron.contract.chat.GetChatMessagesRequest
import ru.zveron.contract.chat.SendMessageRequest
import ru.zveron.contract.chat.chatRouteResponse
import ru.zveron.contract.chat.getChatMessagesResponse
import ru.zveron.contract.chat.model.MessageType
import ru.zveron.contract.chat.receiveMessage
import ru.zveron.exception.ChatException
import ru.zveron.exception.InvalidParamChatException
import ru.zveron.mapper.MessageMapper.messageToResponse
import ru.zveron.mapper.ProtoTypesMapper.toUUID
import ru.zveron.model.dao.ChatRequestContext
import ru.zveron.model.dao.SingleConnectionResponse
import ru.zveron.component.ChatStorage
import ru.zveron.component.MessageStorage
import java.util.*

@Service
class MessageApplicationService(
    private val blacklistClient: BlacklistClient,
    private val snowflakeClient: SnowflakeClient,
    private val chatStorage: ChatStorage,
    private val messageStorage: MessageStorage,
) {

    companion object : KLogging()

    suspend fun getRecentMessagesByChat(
        request: GetChatMessagesRequest,
        context: ChatRequestContext
    ): SingleConnectionResponse {
        logger.debug("Get recent messages from chat: ${request.chatId} {}", keyValue("connection-id", context.connectionId))
        val profileId = context.authorizedProfileId
        val chatId = request.chatId.toUUID(context)

        if (!chatStorage.chatExists(profileId, chatId)) {
            throw ChatException(
                Status.NOT_FOUND,
                "Profile: $profileId does not have chat: $chatId.",
                context
            )
        }
        val messages = messageStorage.getChatRecentMessages(chatId, request.pagination)
            .toList().map { message -> messageToResponse(message) }
        val response = chatRouteResponse {
            getMessagesResponse = getChatMessagesResponse { this.messages.addAll(messages) }
        }

        return SingleConnectionResponse(profileId, response)
    }

    suspend fun sendMessage(request: SendMessageRequest, context: ChatRequestContext): SingleConnectionResponse {
        logger.debug("Send message to chat ${request.chatId} {}", keyValue("connection-id", context.connectionId))
        if (request.type != MessageType.DEFAULT) {
            throw ChatException(Status.UNIMPLEMENTED, "Unimplemented yet", context)
        }
        val profileId = context.authorizedProfileId
        val chatId = request.chatId.toUUID(context)
        val chat = chatStorage.findExact(profileId, chatId)
            ?: throw ChatException(
                Status.NOT_FOUND,
                "Profile: $profileId does not have chat: $chatId.",
                context
            )
        if (blacklistClient.existsInBlacklist(chat.anotherProfileId, profileId)) {
            throw InvalidParamChatException(
                "Cannot send message because authorized profile is in the blacklist of profile ${chat.anotherProfileId}.",
                context
            )
        }

        val messageId = snowflakeClient.fetchUuid()
        val message = messageStorage.saveMessage(
            UUID.fromString(request.chatId),
            messageId,
            profileId,
            request.text,
            request.imagesUrlsList,
        )

        val response =  chatRouteResponse {
            receiveMessage = receiveMessage { this.message = messageToResponse(message) }
        }

        return SingleConnectionResponse(chat.anotherProfileId, response)
    }
}