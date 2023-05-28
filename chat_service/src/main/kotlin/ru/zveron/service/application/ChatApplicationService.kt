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
import ru.zveron.contract.chat.ArticleType
import ru.zveron.contract.chat.AttachLotRequest
import ru.zveron.contract.chat.DetachLotRequest
import ru.zveron.contract.chat.GetChatSummary
import ru.zveron.contract.chat.GetRecentChatsRequest
import ru.zveron.contract.chat.SendEventRequest
import ru.zveron.contract.chat.StartChatRequest
import ru.zveron.contract.chat.chatRouteResponse
import ru.zveron.contract.chat.getRecentChatsResponse
import ru.zveron.contract.chat.model.ChatFolder
import ru.zveron.contract.chat.model.changeMessagesStatusEvent
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
import ru.zveron.model.dao.MultipleConnectionsResponse
import ru.zveron.model.dao.NoneConnectionResponse
import ru.zveron.model.dao.SingleConnectionResponse
import ru.zveron.repository.BatchChatRepository
import ru.zveron.component.ChatStorage
import ru.zveron.component.ConnectionStorage
import ru.zveron.component.MessageStorage
import ru.zveron.model.dao.ChatRequestContext
import java.time.Instant
import java.util.*

@Service
class ChatApplicationService(
    private val lotClient: LotClient,
    private val profileClient: ProfileClient,
    private val blacklistClient: BlacklistClient,
    private val snowflakeClient: SnowflakeClient,
    private val chatStorage: ChatStorage,
    private val batchChatRepository: BatchChatRepository,
    private val messageStorage: MessageStorage,
    private val connectionStorage: ConnectionStorage,
) {

    companion object : KLogging()

    suspend fun getRecentChats(request: GetRecentChatsRequest, context: ChatRequestContext): SingleConnectionResponse {
        logger.debug("Get recent chats")
        val recentChats = chatStorage.getRecentChats(
            context.authorizedProfileId,
            request.pagination,
        )
        val profileIds = mutableListOf<Long>()
        val lotsIds = mutableListOf<Long>()
        val chatIds = mutableListOf<UUID>()

        for (chat in recentChats) {
            profileIds.add(chat.anotherProfileId)
            chat.lotsIds?.apply { lotsIds.addAll(this) }
            chatIds.add(chat.chatId)
        }

        val chats = supervisorScope {
            val profilesSummary = getProfilesSummaryAsync(profileIds)
            val lotsSummary = getLotsSummaryAsync(lotsIds)
            val blacklistRecords = existsInMultipleBlacklistsAsync(context.authorizedProfileId, profileIds)
            val messages = getChatsMessages(chatIds)

            val profileSummaryMap = profilesSummary.awaitProfilesSummary()
            val lotsSummaryMap = lotsSummary.awaitLotsSummary()
            val blacklistsExist = blacklistRecords.awaitMultipleBlacklistRecords()

            recentChats.mapIndexed { index, chat ->
                chatToChatResponse(
                    chat,
                    profileSummaryMap[chat.anotherProfileId],
                    chat.lotsIds?.mapNotNull { id -> lotsSummaryMap[id] },
                    messages[chat.chatId]?.toList(),
                    isBlocked = blacklistsExist.getOrNull(index) ?: true
                )
            }
        }

        val response = chatRouteResponse { getRecentChats = getRecentChatsResponse { this.chats.addAll(chats) } }
        return SingleConnectionResponse(context.authorizedProfileId, response)
    }

    suspend fun attachLotToChat(request: AttachLotRequest, context: ChatRequestContext): NoneConnectionResponse {
        logger.debug("Attach lot: ${request.lotId} to chat: ${request.chatId}")
        val profileId = context.authorizedProfileId
        val chatId = request.chatId.toUUID()
        val chat = getChatIfValid(context.authorizedProfileId, request.interlocutorId, chatId)

        // TODO проверяем что лот принаделжит одному из челов
        if (chat.lotsIds?.contains(request.lotId) == true) {
            throw InvalidParamChatException(
                "Chat with id $chatId already has lot with id: ${request.lotId}.",
            )
        }
        getLotIfValid(request.lotId)

        chatStorage.attachLotToChat(request.lotId, profileId, request.interlocutorId, chatId)

        return NoneConnectionResponse
    }

    suspend fun detachLotFromChat(request: DetachLotRequest, context: ChatRequestContext): NoneConnectionResponse {
        logger.debug("Detach lot: ${request.lotId} to chat: ${request.chatId}")
        val chatId = request.chatId.toUUID()
        val chat = getChatIfValid(context.authorizedProfileId, request.interlocutorId, chatId)

        if (chat.lotsIds?.contains(request.lotId) != true) {
            throw InvalidParamChatException(
                "Chat with id $chatId does not contains lot with id: ${request.lotId}.",
            )
        }

        chatStorage.detachLot(request.lotId, context.authorizedProfileId, request.interlocutorId, chatId)

        return NoneConnectionResponse
    }

    suspend fun sendEvent(request: SendEventRequest, context: ChatRequestContext): SingleConnectionResponse {
        logger.debug("Send event ${request.eventCase.name} to chat: ${request.chatId}")
        val chatId = request.chatId.toUUID()
        val profileId = context.authorizedProfileId
        val interlocutorId = getInterlocutorId(profileId, chatId)

        return when (request.eventCase) {
            SendEventRequest.EventCase.CHANGED_STATUS_EVENT -> {
                val ids = request.changedStatusEvent.idsList.map { it.toUUID() }
                batchChatRepository.markMessagesAsRead(chatId, ids)
                chatStorage.decrementUnreadCounter(profileId, chatId, ids.size)
                val response = chatRouteResponse {
                    receiveEvent =
                        receiveEvent {
                            this.chatId = request.chatId
                            changedStatusEvent = changeMessagesStatusEvent {
                                this.ids.addAll(request.changedStatusEvent.idsList)
                            }
                        }
                }
                SingleConnectionResponse(interlocutorId, response)
            }

            SendEventRequest.EventCase.DISCONNECT_EVENT -> {
                val response = chatRouteResponse {
                    receiveEvent = receiveEvent {
                        this.chatId = request.chatId
                        disconnectEvent = disconnectEvent { lastOnlineFormatted = "Не в сети" }
                    }
                }
                SingleConnectionResponse(interlocutorId, response)
            }

            SendEventRequest.EventCase.NO_PAYLOAD_EVENT -> {
                val response = chatRouteResponse {
                    receiveEvent = receiveEvent {
                        this.chatId = request.chatId
                        noPayloadEvent = request.noPayloadEvent
                    }
                }
                SingleConnectionResponse(interlocutorId, response)
            }

            else -> throw InvalidParamChatException(
                "Unsupported event type: ${request.eventCase.name}."
            )
        }
    }

    suspend fun startChat(request: StartChatRequest, context: ChatRequestContext): MultipleConnectionsResponse {
        logger.debug("Start chat with profile: ${request.interlocutorId}")
        if (request.article.type != ArticleType.LOT) {
            throw ChatException(Status.UNIMPLEMENTED, "Orders are not supported yet in chat service")
        }
        if (context.authorizedProfileId == request.interlocutorId) {
            throw InvalidParamChatException("Cannot start chat with yourself.")
        }
        if (blacklistClient.existsInBlacklist(request.interlocutorId, context.authorizedProfileId)) {
            throw InvalidParamChatException(
                "Cannot start chat with profile: ${request.interlocutorId} because you are in the blacklist.",
            )
        }

        val interlocutorSummary = getProfileSummaryIfValid(request.interlocutorId)
        val lotId = request.article.id
        val lotSummary = getLotIfValid(lotId)

        val chatId = snowflakeClient.fetchUuid()
        val messageId = snowflakeClient.fetchUuid()
        val receivedAt = Instant.now()

        // TODO проверяем что лот принаделжит собеседнику
        chatStorage.createChatsPair(
            context.authorizedProfileId,
            request.interlocutorId,
            chatId,
            lotId,
            messageId,
            request.text,
            receivedAt,
        )

        val interlocutorConnection = connectionStorage.getConnectionWithNewestStatusChange(request.interlocutorId)
        val chat = chat {
            this.chatId = chatId.toString()
            this.interlocutorSummary = interlocutorSummary.toChatSummary(
                interlocutorConnection?.isClosed == false,
                interlocutorConnection?.lastStatusChange,
            )
            messages.add(message {
                id = messageId.toString()
                text = request.text
                isRead = false
                senderId = context.authorizedProfileId
                sentAt = receivedAt.toTimestamp()
            })
            unreadMessages = 0
            lastUpdate = receivedAt.toTimestamp()
            lots.add(lotSummary)
            folder = ChatFolder.NONE
            isBlocked = false
        }

        return MultipleConnectionsResponse(mapOf(
            context.authorizedProfileId to chatRouteResponse {
                chatSummary = receiveChatSummary { this.chat = chat }
            },
            request.interlocutorId to chatRouteResponse {
                receiveMessage = receiveMessage {
                    message = chat.messagesList.first()
                    this.chatId = chat.chatId
                }
            }
        ))
    }

    suspend fun getChatSummary(request: GetChatSummary, context: ChatRequestContext): SingleConnectionResponse {
        val chatId = request.chatId.toUUID()

        val chatResponse = supervisorScope {
            val chat = chatStorage.findExact(context.authorizedProfileId, chatId)
                ?: throw InvalidParamChatException(
                    "Profile with id ${context.authorizedProfileId} does not have chat with id: $chatId."
                )

            val profilesSummary = getProfilesSummaryAsync(listOf(chat.anotherProfileId))
            val lotsSummary = chat.lotsIds?.let { getLotsSummaryAsync(it.toList()) }
            val blacklistRecord = existsInBlacklist(chat.anotherProfileId, context.authorizedProfileId)
            val messages = getChatsMessages(listOf(chatId))

            val profileSummaryMap = profilesSummary.awaitProfilesSummary()
            val lotsSummaryMap = lotsSummary?.awaitLotsSummary() ?: emptyMap()
            val isInBlacklist = blacklistRecord.awaitBlacklistRecord()

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

    private fun CoroutineScope.getProfilesSummaryAsync(ids: List<Long>) =
        async(CoroutineName("Get-Profiles-Summary-Coroutine")) {
            profileClient.getProfilesSummary(ids)
        }

    private fun CoroutineScope.getLotsSummaryAsync(ids: List<Long>) =
        async(CoroutineName("Get-Lots-Summary-Coroutine")) {
            lotClient.getLotsById(ids)
        }

    private fun CoroutineScope.existsInMultipleBlacklistsAsync(targetProfileId: Long, ownersIds: List<Long>) =
        async(CoroutineName("Exists-In-Multiple-Blacklists-Coroutine")) {
            blacklistClient.existsInMultipleBlacklists(targetProfileId, ownersIds)
        }

    private fun CoroutineScope.existsInBlacklist(blacklistOwnerId: Long, anotherProfileId: Long) =
        async(CoroutineName("Exists-In-Blacklist-Coroutine")) {
            blacklistClient.existsInBlacklist(blacklistOwnerId, anotherProfileId)
        }

    private fun getChatsMessages(chatsIds: List<UUID>) =
        chatsIds.associateWith {
            messageStorage.getChatRecentMessages(it).map { message -> messageToResponse(message) }
        }

    private suspend fun Deferred<List<ProfileSummary>>.awaitProfilesSummary(): Map<Long, ru.zveron.contract.chat.model.ProfileSummary> =
        try {
            val profiles = await()
            profiles.associate {
                // TODO очень плохое решение, надо бы что-то другое придумать
                //  https://zveron.atlassian.net/browse/ZV-423?atlOrigin=eyJpIjoiNDcyY2MwMmU2MzY1NDE2MGE4ZmU4YWM3ZTI0MmViMWMiLCJwIjoiaiJ9
                val interlocutorConnection = connectionStorage.getConnectionWithNewestStatusChange(it.id)

                it.id to it.toChatSummary(
                    interlocutorConnection?.isClosed == false,
                    interlocutorConnection?.lastStatusChange,
                )
            }
        } catch (ex: StatusException) {
            logger.error(
                "Cannot load profiles summary. Got ${ex.status} from profile service. Message: ${ex.message}"
            )
            emptyMap()
        }

    private suspend fun Deferred<List<Lot>>.awaitLotsSummary(): Map<Long, Lot> =
        try {
            val lots = await()
            lots.associateBy { it.id }
        } catch (ex: StatusException) {
            logger.error("Cannot load lots summary. Got ${ex.status} from lot service. Message: ${ex.message}")
            emptyMap()
        }

    private suspend fun Deferred<List<Boolean>>.awaitMultipleBlacklistRecords(): List<Boolean> =
        try {
            await()
        } catch (ex: StatusException) {
            logger.error(
                "Cannot load blacklist records. Got ${ex.status} from blacklist service. Message: ${ex.message}"
            )
            emptyList()
        }

    private suspend fun Deferred<Boolean>.awaitBlacklistRecord(): Boolean =
        try {
            await()
        } catch (ex: StatusException) {
            logger.error(
                "Cannot get blacklist record. Got ${ex.status} from blacklist service. Message: ${ex.message}"
            )
            false
        }

    private fun StatusException.wrapIfAppropriate(lotId: Long): ChatException {
        return when (status) {
            Status.NOT_FOUND -> ChatException(Status.NOT_FOUND, "lot with id: $lotId does not exists")
            Status.INVALID_ARGUMENT -> InvalidParamChatException("lot id: $lotId is invalid")
            else -> ChatException(
                Status.FAILED_PRECONDITION,
                "Cannot validate lot: $lotId. Got $status from lot service.",
            )
        }
    }

    private suspend fun getChatIfValid(
        authorizedProfileId: Long,
        anotherProfileId: Long,
        chatId: UUID,
    ): ru.zveron.model.entity.Chat {
        val chat = chatStorage.findExact(authorizedProfileId, chatId)
            ?: throw InvalidParamChatException(
                "Chat with id: $chatId does not exists for user $authorizedProfileId",
            )

        if (chat.anotherProfileId != anotherProfileId) {
            throw InvalidParamChatException(
                "Chat with id $chatId does not contains user with id: $anotherProfileId",
            )
        }

        return chat
    }

    private suspend fun getProfileSummaryIfValid(profileId: Long): ProfileSummary =
        try {
            profileClient.getProfilesSummary(listOf(profileId)).firstOrNull()
                ?: throw InvalidParamChatException("Profile with id $profileId does not exists")
        } catch (ex: StatusException) {
            logger.error(ex.message)
            throw ex.wrapIfAppropriate(profileId)
        }

    private suspend fun getLotIfValid(lotId: Long): Lot =
        try {
            lotClient.getLotsById(listOf(lotId)).firstOrNull()
                ?: throw InvalidParamChatException("Lot with id $lotId does not exists")
        } catch (ex: StatusException) {
            logger.error(ex.message)
            throw ex.wrapIfAppropriate(lotId)
        }

    private suspend fun getInterlocutorId(profileId: Long, chatId: UUID) =
        chatStorage.getInterlocutorId(profileId, chatId)
            ?: throw InvalidParamChatException(
                "Chat: $chatId does not exists for profile: $profileId."
            )
}