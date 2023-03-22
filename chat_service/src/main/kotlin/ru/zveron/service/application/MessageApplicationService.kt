package ru.zveron.service.application

import io.grpc.Status
import kotlinx.coroutines.flow.toList
import mu.KLogging
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
import ru.zveron.service.domain.ChatDomainService
import ru.zveron.service.domain.MessageDomainService
import ru.zveron.util.MessageFormatter.appendContext
import java.util.*

@Service
class MessageApplicationService(
    private val blacklistClient: BlacklistClient,
    private val snowflakeClient: SnowflakeClient,
    private val chatDomainService: ChatDomainService,
    private val messageDomainService: MessageDomainService,
) {

    companion object : KLogging()

    suspend fun getRecentMessagesByChat(
        request: GetChatMessagesRequest,
        context: ChatRequestContext
    ): SingleConnectionResponse {
        logger.info("Get recent messages from chat: ${request.chatId}.".appendContext(context))
        val profileId = context.authorizedProfileId
        val chatId = request.chatId.toUUID()
        if (!chatDomainService.chatExists(profileId, chatId)) {
            throw ChatException(
                Status.NOT_FOUND,
                "Profile: $profileId does not have chat: $chatId.".appendContext(context)
            )
        }
        val messages = messageDomainService.getChatRecentMessages(chatId, request.pagination)
            .toList().map { message -> messageToResponse(message) }
        val response = chatRouteResponse {
            getMessagesResponse = getChatMessagesResponse { this.messages.addAll(messages) }
        }

        return SingleConnectionResponse(profileId, response)
    }

    suspend fun sendMessage(request: SendMessageRequest, context: ChatRequestContext): SingleConnectionResponse {
        logger.info("Send message to chat ${request.chatId}.".appendContext(context))
        if (request.type != MessageType.DEFAULT) {
            throw ChatException(Status.UNIMPLEMENTED, "Unimplemented yet")
        }
        val profileId = context.authorizedProfileId
        val chatId = request.chatId.toUUID()
        val chat = chatDomainService.findExact(profileId, chatId)
            ?: throw ChatException(
                Status.NOT_FOUND,
                "Profile: $profileId does not have chat: $chatId.".appendContext(context)
            )
        if (blacklistClient.existsInBlacklist(chat.anotherProfileId, profileId)) {
            throw InvalidParamChatException(
                "Cannot send message because authorized profile is in the blacklist of profile ${chat.anotherProfileId}."
                    .appendContext(context)
            )
        }

        val messageId = snowflakeClient.fetchUuid()
        val message = messageDomainService.saveMessage(
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