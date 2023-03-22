package ru.zveron.service.application

import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.supervisorScope
import mu.KLogging
import org.springframework.stereotype.Service
import ru.zveron.client.blacklist.BlacklistClient
import ru.zveron.client.lot.LotClient
import ru.zveron.client.profile.ProfileClient
import ru.zveron.client.snowflake.SnowflakeClient
import ru.zveron.contract.chat.AttachLotRequest
import ru.zveron.contract.chat.DetachLotRequest
import ru.zveron.contract.chat.GetChatSummary
import ru.zveron.contract.chat.GetRecentChatsRequest
import ru.zveron.contract.chat.SendEventRequest
import ru.zveron.contract.chat.StartChatRequest
import ru.zveron.contract.chat.chatRouteResponse
import ru.zveron.contract.chat.getRecentChatsResponse
import ru.zveron.contract.chat.model.ChatFolder
import ru.zveron.contract.chat.model.chat
import ru.zveron.contract.chat.model.disconnectEvent
import ru.zveron.contract.chat.model.message
import ru.zveron.contract.chat.receiveChatSummary
import ru.zveron.contract.chat.receiveEvent
import ru.zveron.contract.chat.receiveMessage
import ru.zveron.contract.core.Lot
import ru.zveron.contract.profile.ProfileSummary
import ru.zveron.exception.ChatException
import ru.zveron.exception.InvalidParamChatException
import ru.zveron.mapper.ChatMapper.chatToChatResponse
import ru.zveron.mapper.ChatMapper.toChatSummary
import ru.zveron.mapper.MessageMapper.messageToResponse
import ru.zveron.mapper.ProtoTypesMapper.toTimestamp
import ru.zveron.mapper.ProtoTypesMapper.toUUID
import ru.zveron.model.dao.ChatRequestContext
import ru.zveron.model.dao.ChatRouteResponseWrapper
import ru.zveron.model.dao.MultipleConnectionsResponse
import ru.zveron.model.dao.NoneConnectionResponse
import ru.zveron.model.dao.SingleConnectionResponse
import ru.zveron.repository.BatchChatRepository
import ru.zveron.service.domain.ChatDomainService
import ru.zveron.service.domain.MessageDomainService
import ru.zveron.util.MessageFormatter.appendContext
import java.time.Instant
import java.util.*

@Service
class ChatApplicationService(
    private val lotClient: LotClient,
    private val profileClient: ProfileClient,
    private val blacklistClient: BlacklistClient,
    private val snowflakeClient: SnowflakeClient,
    private val chatDomainService: ChatDomainService,
    private val batchChatRepository: BatchChatRepository,
    private val messageDomainService: MessageDomainService,
) {

    companion object : KLogging()

    suspend fun getRecentChats(request: GetRecentChatsRequest, context: ChatRequestContext): SingleConnectionResponse {
        logger.info("Get recent chats for connection ${context.connectionId}")
        val recentChats = chatDomainService.getRecentChats(
            context.authorizedProfileId,
            request.pagination,
        ).toList()
        val profileIds = recentChats.map { it.anotherProfileId }
        val lotsIds = recentChats.flatMap { it.lotsIds ?: emptyList() }
        val chatIds = recentChats.map { it.chatId }

        val chats = supervisorScope {
            val profilesSummary = getProfilesSummary(profileIds)
            val lotsSummary = getLotsSummary(lotsIds)
            val messages = getChatsMessages(chatIds)

            val profileSummaryMap = profilesSummary.awaitProfilesSummary(context)
            val lotsSummaryMap = lotsSummary.awaitLotsSummary(context)

            recentChats.map {
                chatToChatResponse(
                    it,
                    profileSummaryMap[it.anotherProfileId],
                    it.lotsIds?.mapNotNull { id -> lotsSummaryMap[id] },
                    messages[it.chatId]?.toList(),
                    isBlocked = false // TODO
                )
            }
        }

        val response = chatRouteResponse { getRecentChats = getRecentChatsResponse { this.chats.addAll(chats) } }
        return SingleConnectionResponse(context.authorizedProfileId, response)
    }

    suspend fun attachLotToChat(request: AttachLotRequest, context: ChatRequestContext): NoneConnectionResponse {
        logger.info("Attach lot: ${request.lotId} to chat: ${request.chatId}.".appendContext(context))
        val profileId = context.authorizedProfileId
        val chatId = request.chatId.toUUID()
        val chat = getChatIfValid(context.authorizedProfileId, request.interlocutorId, chatId, context)

        if (chat.lotsIds?.contains(request.lotId) == true) {
            throw InvalidParamChatException(
                "Chat with id $chatId already has lot with id: ${request.lotId}.".appendContext(
                    context
                )
            )
        }
        getLotIfValid(request.lotId, context)

        chatDomainService.attachLotToChat(request.lotId, profileId, request.interlocutorId, chatId)

        return NoneConnectionResponse
    }

    suspend fun detachLotFromChat(request: DetachLotRequest, context: ChatRequestContext): NoneConnectionResponse {
        logger.info("Detach lot: ${request.lotId} to chat: ${request.chatId}.".appendContext(context))
        val chatId = request.chatId.toUUID()
        val chat = getChatIfValid(context.authorizedProfileId, request.interlocutorId, chatId, context)

        if (chat.lotsIds?.contains(request.lotId) != true) {
            throw InvalidParamChatException(
                "Chat with id $chatId does not contains lot with id: ${request.lotId}.".appendContext(
                    context
                )
            )
        }

        chatDomainService.detachLot(request.lotId, context.authorizedProfileId, request.interlocutorId, chatId)

        return NoneConnectionResponse
    }

    suspend fun sendEvent(request: SendEventRequest, context: ChatRequestContext): ChatRouteResponseWrapper {
        logger.info("Send event ${request.eventCase.name} to chat: ${request.chatId}.".appendContext(context))
        val chatId = request.chatId.toUUID()
        val profileId = context.authorizedProfileId

        return when (request.eventCase) {
            SendEventRequest.EventCase.CHANGED_STATUS_EVENT -> {
                val ids = request.changedStatusEvent.idsList.map { it.toUUID() }
                batchChatRepository.markMessagesAsRead(chatId, ids)
                NoneConnectionResponse
            }

            SendEventRequest.EventCase.DISCONNECT_EVENT -> {
                val interlocutorId = getInterlocutorId(profileId, chatId, context)
                val response = chatRouteResponse {
                    receiveEvent =
                        receiveEvent { disconnectEvent = disconnectEvent { lastOnlineFormatted = "Не в сети" } }
                }
                SingleConnectionResponse(interlocutorId, response)
            }

            SendEventRequest.EventCase.NO_PAYLOAD_EVENT -> {
                val interlocutorId = getInterlocutorId(profileId, chatId, context)
                val response = chatRouteResponse {
                    receiveEvent = receiveEvent { noPayloadEvent = request.noPayloadEvent }
                }
                SingleConnectionResponse(interlocutorId, response)
            }

            else -> throw InvalidParamChatException(
                "Unsupported event type: ${request.eventCase.name}.".appendContext(context)
            )
        }
    }

    suspend fun startChat(request: StartChatRequest, context: ChatRequestContext): MultipleConnectionsResponse {
        logger.info("Start chat with profile: ${request.interlocutorId}.".appendContext(context))
        if (context.authorizedProfileId == request.interlocutorId) {
            throw InvalidParamChatException("Cannot start chat with yourself.".appendContext(context))
        }
        if (blacklistClient.existsInBlacklist(request.interlocutorId, context.authorizedProfileId)) {
            throw InvalidParamChatException(
                "Cannot start chat with profile: ${request.interlocutorId} because you are in the blacklist."
                    .appendContext(context)
            )
        }
        val lotSummary = getLotIfValid(request.lotId, context)

        val chat = supervisorScope {
            val interlocutorSummary = getProfilesSummary(listOf(request.interlocutorId))
            val chatId = snowflakeClient.fetchUuid()
            val messageId = snowflakeClient.fetchUuid()
            val receivedAt = Instant.now()

            chatDomainService.createChatsPair(
                context.authorizedProfileId,
                request.interlocutorId,
                chatId,
                request.lotId,
                messageId,
                request.text,
                receivedAt,
            )

            chat {
                this.chatId = chatId.toString()
                this.interlocutorSummary = interlocutorSummary.await().first().toChatSummary()
                messages.add(message {
                    id = messageId.toString()
                    text = request.text
                    isRead = false
                    senderId = context.authorizedProfileId
                    sentAt = receivedAt.toTimestamp()
                })
                unreadMessages = 1
                lastUpdate = receivedAt.toTimestamp()
                lots.add(lotSummary)
                folder = ChatFolder.NONE
                isBlocked = false
            }
        }

        return MultipleConnectionsResponse(mapOf(
            context.authorizedProfileId to chatRouteResponse { chatSummary = receiveChatSummary { this.chat = chat } },
            request.interlocutorId to chatRouteResponse { receiveMessage = receiveMessage { message = chat.messagesList.first() } }
        ))
    }

    suspend fun getChatSummary(request: GetChatSummary, context: ChatRequestContext): SingleConnectionResponse {
        val chatId = request.chatId.toUUID()

        val chatResponse = supervisorScope {
            val chat = chatDomainService.findExact(context.authorizedProfileId, chatId)
                ?: throw InvalidParamChatException(
                    "Profile with id ${context.authorizedProfileId} does not have chat with id: $chatId."
                        .appendContext(context)
                )

            val profilesSummary = getProfilesSummary(listOf(chat.anotherProfileId))
            val lotsSummary = chat.lotsIds?.let { getLotsSummary(it.toList()) }
            val blacklistRecord = existsInBlacklist(chat.anotherProfileId, context.authorizedProfileId)
            val messages = getChatsMessages(listOf(chatId))

            val profileSummaryMap = profilesSummary.awaitProfilesSummary(context)
            val lotsSummaryMap = lotsSummary?.awaitLotsSummary(context) ?: emptyMap()
            val isInBlacklist = blacklistRecord.awaitBlacklistRecord(context)

            chatToChatResponse(
                chat,
                profileSummaryMap[chat.anotherProfileId],
                chat.lotsIds?.mapNotNull { id -> lotsSummaryMap[id] },
                messages[chat.chatId]?.toList(),
                isBlocked = isInBlacklist,
            )
        }

        val response = chatRouteResponse { chatSummary = receiveChatSummary { chat = chatResponse } }

        return SingleConnectionResponse(context.authorizedProfileId, response)
    }

    private fun CoroutineScope.getProfilesSummary(ids: List<Long>) =
        async(CoroutineName("Get-Profiles-Summary-Coroutine")) {
            profileClient.getProfilesSummary(ids)
        }

    private fun CoroutineScope.getLotsSummary(ids: List<Long>) =
        async(CoroutineName("Get-Lots-Summary-Coroutine")) {
            lotClient.getLotsById(ids)
        }

    private fun CoroutineScope.existsInBlacklist(blacklistOwnerId: Long, anotherProfileId: Long) =
        async(CoroutineName("Exists-In-Blacklist-Coroutine")) {
            blacklistClient.existsInBlacklist(blacklistOwnerId, anotherProfileId)
        }

    private fun getChatsMessages(chatsIds: List<UUID>) =
        chatsIds.associateWith {
            messageDomainService.getChatRecentMessages(it).map { message -> messageToResponse(message) }
        }

    private suspend fun Deferred<List<ProfileSummary>>.awaitProfilesSummary(context: ChatRequestContext): Map<Long, ru.zveron.contract.chat.model.ProfileSummary> =
        try {
            val profiles = await()
            profiles.associate { it.id to it.toChatSummary() }
        } catch (ex: StatusException) {
            logger.error(
                "Cannot load profiles summary. Got ${ex.status} from profile service. Message: ${ex.message}"
                    .appendContext(context)
            )
            emptyMap()
        }

    private suspend fun Deferred<List<Lot>>.awaitLotsSummary(context: ChatRequestContext): Map<Long, Lot> =
        try {
            val profiles = await()
            profiles.associateBy { it.id }
        } catch (ex: StatusException) {
            logger.error(
                "Cannot load lots summary. Got ${ex.status} from lot service. Message: ${ex.message}"
                    .appendContext(context)
            )
            emptyMap()
        }

    private suspend fun Deferred<Boolean>.awaitBlacklistRecord(context: ChatRequestContext): Boolean =
        try {
            await()
        } catch (ex: StatusException) {
            logger.error(
                "Cannot get blacklist record. Got ${ex.status} from blacklist service. Message: ${ex.message}"
                    .appendContext(context)
            )
            false
        }

    private fun StatusException.wrapIfAppropriate(lotId: Long, context: ChatRequestContext): ChatException {
        return when (status) {
            Status.NOT_FOUND -> ChatException(Status.NOT_FOUND, "lot with id: $lotId does not exists")
            Status.INVALID_ARGUMENT -> InvalidParamChatException("lot id: $lotId is invalid")
            else -> ChatException(
                Status.FAILED_PRECONDITION,
                "Cannot validate lot: $lotId. Got $status from lot service."
                    .appendContext(context)
            )
        }
    }

    private suspend fun getChatIfValid(
        authorizedProfileId: Long,
        anotherProfileId: Long,
        chatId: UUID,
        context: ChatRequestContext,
    ): ru.zveron.model.entity.Chat {
        val chat = chatDomainService.findExact(authorizedProfileId, chatId)
            ?: throw InvalidParamChatException(
                "Chat with id: $chatId does not exists for user $authorizedProfileId"
                    .appendContext(context)
            )

        if (chat.anotherProfileId != anotherProfileId) {
            throw InvalidParamChatException(
                "Chat with id $chatId does not contains user with id: $anotherProfileId"
                    .appendContext(context)
            )
        }

        return chat
    }

    private suspend fun getLotIfValid(lotId: Long, context: ChatRequestContext): Lot =
        try {
            lotClient.getLotsById(listOf(lotId)).firstOrNull()
                ?: throw InvalidParamChatException("Lot with id $lotId does not exists".appendContext(context))
        } catch (ex: StatusException) {
            logger.error(ex.message)
            throw ex.wrapIfAppropriate(lotId, context)
        }

    private suspend fun getInterlocutorId(profileId: Long, chatId: UUID, context: ChatRequestContext) =
        chatDomainService.getInterlocutorId(profileId, chatId)
            ?: throw InvalidParamChatException(
                "Chat: $chatId does not exists for profile: $profileId.".appendContext(context)
            )
}