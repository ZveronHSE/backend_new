package ru.zveron.service.application

import com.datastax.oss.driver.api.core.uuid.Uuids
import io.grpc.Status
import io.grpc.StatusException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.mockk.coEvery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.ChatServiceApplicationTest
import ru.zveron.common.assertion.ChatAssertions.newChatShouldBe
import ru.zveron.common.assertion.ChatAssertions.responseShouldBe
import ru.zveron.common.generator.ChatGenerator
import ru.zveron.common.generator.ChatGenerator.generateChatResponse
import ru.zveron.common.generator.LotGenerator.generateLot
import ru.zveron.common.generator.MessageGenerator.generateMessage
import ru.zveron.common.generator.PrimitivesGenerator
import ru.zveron.common.generator.PrimitivesGenerator.generateLongs
import ru.zveron.common.generator.PrimitivesGenerator.generateString
import ru.zveron.common.generator.ProfileSummaryGenerator.generateProfile
import ru.zveron.contract.chat.ArticleType
import ru.zveron.contract.chat.article
import ru.zveron.contract.chat.attachLotRequest
import ru.zveron.contract.chat.detachLotRequest
import ru.zveron.contract.chat.getRecentChatsRequest
import ru.zveron.contract.chat.getRecentChatsResponse
import ru.zveron.contract.chat.model.NoPayloadEventType
import ru.zveron.contract.chat.model.changeMessagesStatusEvent
import ru.zveron.contract.chat.model.chatPagination
import ru.zveron.contract.chat.model.disconnectEvent
import ru.zveron.contract.chat.model.noPayloadEvent
import ru.zveron.contract.chat.receiveChatSummary
import ru.zveron.contract.chat.sendEventRequest
import ru.zveron.contract.chat.startChatRequest
import ru.zveron.exception.ChatException
import ru.zveron.exception.InvalidParamChatException
import ru.zveron.library.grpc.interceptor.model.MetadataElement
import ru.zveron.library.grpc.model.Metadata
import ru.zveron.library.grpc.util.GrpcUtils
import ru.zveron.mapper.ChatMapper.toChatSummary
import ru.zveron.mapper.MessageMapper.messageToResponse
import ru.zveron.mapper.ProtoTypesMapper.toTimestamp
import ru.zveron.model.dao.ChatRequestContext
import ru.zveron.repository.ChatRepository
import ru.zveron.repository.MessageRepository
import java.time.Instant

class ChatApplicationServiceTest : ChatServiceApplicationTest() {

    @Autowired
    lateinit var messageRepository: MessageRepository

    @Autowired
    lateinit var chatRepository: ChatRepository

    @Autowired
    lateinit var chatApplicationService: ChatApplicationService

    @Test
    fun `getRecentChats without pagination`() {
        val timestamp = Instant.now()
        val (msg1, msg2, msg3) = PrimitivesGenerator.generateNTimeUuids(3)
        val (user1, user2, user3, user4, lot1) = generateLongs(5)
        val chat1 = ChatGenerator.generateChat(user1, user2, lastUpdate = timestamp.minusSeconds(2))
        val chat2 =
            ChatGenerator.generateChat(user1, user3, lotsIds = setOf(lot1), lastUpdate = timestamp.minusSeconds(1))
        val chat3 = ChatGenerator.generateChat(user1, user4, lastUpdate = timestamp)
        val profile2 = generateProfile(user2)
        val profile3 = generateProfile(user3)
        val profile4 = generateProfile(user4)
        val lot = generateLot(lot1)
        val message1 = generateMessage(chat1.chatId, msg1, user2)
        val message2 = generateMessage(chat2.chatId, msg2, user3)
        val message3 = generateMessage(chat3.chatId, msg3, user4)
        val request = getRecentChatsRequest {}
        val expectedResponse = getRecentChatsResponse {
            chats.add(
                generateChatResponse(
                    chat3,
                    profile4.toChatSummary(),
                    emptyList(),
                    listOf(messageToResponse(message3)),
                )
            )
            chats.add(
                generateChatResponse(
                    chat2,
                    profile3.toChatSummary(),
                    listOf(lot),
                    listOf(messageToResponse(message2)),
                    isBlocked = true,
                )
            )
            chats.add(
                generateChatResponse(
                    chat1,
                    profile2.toChatSummary(),
                    emptyList(),
                    listOf(messageToResponse(message1)),
                )
            )
        }

        coEvery {
            profileClient.getProfilesSummary(listOf(user4, user3, user2))
        } returns listOf(profile4, profile3, profile2)
        coEvery {
            lotClient.getLotsById(listOf(lot1))
        } returns listOf(lot)
        coEvery {
            blacklistClient.existsInMultipleBlacklists(user1, listOf(user4, user3, user2))
        } returns listOf(false, true, false)

        runBlocking(MetadataElement(Metadata(user1))) {
            chatRepository.save(chat1)
            chatRepository.save(chat2)
            chatRepository.save(chat3)
            messageRepository.save(message1)
            messageRepository.save(message2)
            messageRepository.save(message3)

            chatApplicationService.getRecentChats(
                request,
                defaultContext()
            ).responseBody.getRecentChats responseShouldBe expectedResponse
        }
    }

    @Test
    fun `getRecentChats test with pagination`() {
        val timestamp = Instant.now()
        val (msg1, msg2, msg3) = PrimitivesGenerator.generateNTimeUuids(3)
        val (user1, user2, user3, user4, lot1) = generateLongs(5)
        val chat1 = ChatGenerator.generateChat(user1, user2, lastUpdate = timestamp.minusSeconds(2))
        val chat2 =
            ChatGenerator.generateChat(user1, user3, lotsIds = setOf(lot1), lastUpdate = timestamp.minusSeconds(1))
        val chat3 = ChatGenerator.generateChat(user1, user4, lastUpdate = timestamp)
        val profile2 = generateProfile(user2)
        val profile3 = generateProfile(user3)
        val lot = generateLot(lot1)
        val message1 = generateMessage(chat1.chatId, msg1, user2)
        val message2 = generateMessage(chat2.chatId, msg2, user3)
        val message3 = generateMessage(chat3.chatId, msg3, user4)
        val request = getRecentChatsRequest {
            pagination = chatPagination {
                size = 3
                timeBefore = timestamp.toTimestamp()
            }
        }
        val expectedResponse = getRecentChatsResponse {
            chats.add(
                generateChatResponse(
                    chat2,
                    profile3.toChatSummary(),
                    listOf(lot),
                    listOf(messageToResponse(message2)),
                    isBlocked = true,
                )
            )
            chats.add(
                generateChatResponse(
                    chat1,
                    profile2.toChatSummary(),
                    emptyList(),
                    listOf(messageToResponse(message1)),
                )
            )
        }

        coEvery {
            profileClient.getProfilesSummary(listOf(user3, user2))
        } returns listOf(profile3, profile2)
        coEvery {
            lotClient.getLotsById(listOf(lot1))
        } returns listOf(lot)
        coEvery {
            blacklistClient.existsInMultipleBlacklists(user1, listOf(user3, user2))
        } returns listOf(true, false)

        runBlocking(MetadataElement(Metadata(user1))) {
            chatRepository.save(chat1)
            chatRepository.save(chat2)
            chatRepository.save(chat3)
            messageRepository.save(message1)
            messageRepository.save(message2)
            messageRepository.save(message3)

            chatApplicationService.getRecentChats(
                request,
                defaultContext()
            ).responseBody.getRecentChats responseShouldBe expectedResponse
        }
    }

    @Test
    fun `attachLotToChat when chat exists`() {
        val (user1, user2, lot1) = generateLongs(3)
        val chat1 = ChatGenerator.generateChat(user1, user2)
        val request = attachLotRequest {
            lotId = lot1
            chatId = chat1.chatId.toString()
            interlocutorId = user2
        }
        val lot = generateLot(lot1)

        coEvery {
            lotClient.getLotsById(listOf(lot1))
        } returns listOf(lot)

        runBlocking(MetadataElement(Metadata(user1))) {
            chatRepository.save(chat1)

            chatApplicationService.attachLotToChat(request, defaultContext())

            chatRepository.findByProfileIdAndChatId(user1, chat1.chatId)?.lotsIds shouldContainExactly listOf(lot1)
        }
    }

    @Test
    fun `attachLotToChat when chat does not exists`() {
        val (user1, user2, lot1) = generateLongs(3)
        val chat1 = ChatGenerator.generateChat(user1, user2, lotsIds = setOf(lot1))
        val request = attachLotRequest {
            lotId = lot1
            chatId = chat1.chatId.toString()
            interlocutorId = user2
        }
        shouldThrow<InvalidParamChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                chatApplicationService.attachLotToChat(request, defaultContext())
            }
        }.message shouldStartWith "Chat with id: ${chat1.chatId} does not exists for user $user1"
    }

    @Test
    fun `attachLotToChat when chat does not contain interlocutor`() {
        val (user1, user2, user3, lot1) = generateLongs(4)
        val chat1 = ChatGenerator.generateChat(user1, user2, lotsIds = setOf(lot1))
        val request = attachLotRequest {
            lotId = lot1
            chatId = chat1.chatId.toString()
            interlocutorId = user3
        }
        shouldThrow<InvalidParamChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                chatRepository.save(chat1)

                chatApplicationService.attachLotToChat(request, defaultContext())
            }
        }.message shouldStartWith "Chat with id ${chat1.chatId} does not contains user with id: $user3"
    }

    @Test
    fun `attachLotToChat when chat already has lot`() {
        val (user1, user2, lot1) = generateLongs(3)
        val chat1 = ChatGenerator.generateChat(user1, user2, lotsIds = setOf(lot1))
        val request = attachLotRequest {
            lotId = lot1
            chatId = chat1.chatId.toString()
            interlocutorId = user2
        }
        shouldThrow<InvalidParamChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                chatRepository.save(chat1)

                chatApplicationService.attachLotToChat(request, defaultContext())
            }
        }.message shouldStartWith "Chat with id ${chat1.chatId} already has lot with id: $lot1"
    }

    @Test
    fun `attachLotToChat when lot service throws exception`() {
        val (user1, user2, lot1, lot2, lot3) = generateLongs(5)
        val chat1 = ChatGenerator.generateChat(user1, user2)
        val request1 = attachLotRequest {
            lotId = lot1
            chatId = chat1.chatId.toString()
            interlocutorId = user2
        }
        val request2 = attachLotRequest {
            lotId = lot2
            chatId = chat1.chatId.toString()
            interlocutorId = user2
        }
        val request3 = attachLotRequest {
            lotId = lot3
            chatId = chat1.chatId.toString()
            interlocutorId = user2
        }

        coEvery {
            lotClient.getLotsById(listOf(lot1))
        } throws StatusException(Status.INVALID_ARGUMENT)
        coEvery {
            lotClient.getLotsById(listOf(lot2))
        } throws StatusException(Status.NOT_FOUND)
        coEvery {
            lotClient.getLotsById(listOf(lot3))
        } throws StatusException(Status.INTERNAL)

        runBlocking {
            chatRepository.save(chat1)
        }

        shouldThrow<ChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                chatApplicationService.attachLotToChat(request1, defaultContext())
            }
        }.status shouldBe Status.INVALID_ARGUMENT
        shouldThrow<ChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                chatApplicationService.attachLotToChat(request2, defaultContext())
            }
        }.status shouldBe Status.NOT_FOUND
        shouldThrow<ChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                chatApplicationService.attachLotToChat(request3, defaultContext())
            }
        }.status shouldBe Status.FAILED_PRECONDITION
    }

    @Test
    fun `detachLotFromChat when chat exists`() {
        val (user1, user2, lot1) = generateLongs(3)
        val chat1 = ChatGenerator.generateChat(user1, user2, lotsIds = setOf(lot1))
        val request = detachLotRequest {
            lotId = lot1
            chatId = chat1.chatId.toString()
            interlocutorId = user2
        }

        runBlocking(MetadataElement(Metadata(user1))) {
            chatRepository.save(chat1)

            chatApplicationService.detachLotFromChat(request, defaultContext())

            chatRepository.findByProfileIdAndChatId(user1, chat1.chatId)?.lotsIds shouldBe null
        }
    }

    @Test
    fun `detachLotFromChat when chat does not exists`() {
        val (user1, user2, lot1) = generateLongs(3)
        val chat1 = ChatGenerator.generateChat(user1, user2, lotsIds = setOf(lot1))
        val request = detachLotRequest {
            lotId = lot1
            chatId = chat1.chatId.toString()
            interlocutorId = user2
        }
        shouldThrow<InvalidParamChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                chatApplicationService.detachLotFromChat(request, defaultContext())
            }
        }.message shouldStartWith "Chat with id: ${chat1.chatId} does not exists for user $user1"
    }

    @Test
    fun `detachLotFromChat when chat does not contain interlocutor`() {
        val (user1, user2, user3, lot1) = generateLongs(4)
        val chat1 = ChatGenerator.generateChat(user1, user2, lotsIds = setOf(lot1))
        val request = detachLotRequest {
            lotId = lot1
            chatId = chat1.chatId.toString()
            interlocutorId = user3
        }
        shouldThrow<InvalidParamChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                chatRepository.save(chat1)

                chatApplicationService.detachLotFromChat(request, defaultContext())
            }
        }.message shouldStartWith "Chat with id ${chat1.chatId} does not contains user with id: $user3"
    }

    @Test
    fun `detachLotFromChat when chat does not have lot`() {
        val (user1, user2, lot1) = generateLongs(3)
        val chat1 = ChatGenerator.generateChat(user1, user2)
        val request = detachLotRequest {
            lotId = lot1
            chatId = chat1.chatId.toString()
            interlocutorId = user2
        }
        shouldThrow<InvalidParamChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                chatRepository.save(chat1)

                chatApplicationService.detachLotFromChat(request, defaultContext())
            }
        }.message shouldStartWith "Chat with id ${chat1.chatId} does not contains lot with id: ${request.lotId}"
    }

    @Test
    fun `getChatSummary when chat exists`() {
        val timestamp = Instant.now()
        val (msg1) = PrimitivesGenerator.generateNTimeUuids(1)
        val (user1, user2, lot1) = generateLongs(3)
        val chat1 =
            ChatGenerator.generateChat(user1, user2, lotsIds = setOf(lot1), lastUpdate = timestamp.minusSeconds(2))
        val profile2 = generateProfile(user2)
        val lot = generateLot(lot1)
        val message1 = generateMessage(chat1.chatId, msg1, user2)
        val request = ru.zveron.contract.chat.getChatSummary {
            chatId = chat1.chatId.toString()
        }
        val expectedResponse = receiveChatSummary {
            chat =
                generateChatResponse(
                    chat1,
                    profile2.toChatSummary(),
                    listOf(lot),
                    listOf(messageToResponse(message1)),
                    isBlocked = true
                )
        }

        coEvery {
            profileClient.getProfilesSummary(listOf(user2))
        } returns listOf(profile2)
        coEvery {
            lotClient.getLotsById(listOf(lot1))
        } returns listOf(lot)
        coEvery {
            blacklistClient.existsInBlacklist(user2, user1)
        } returns true

        runBlocking(MetadataElement(Metadata(user1))) {
            chatRepository.save(chat1)
            messageRepository.save(message1)

            chatApplicationService.getChatSummary(
                request,
                defaultContext()
            ).responseBody.chatSummary responseShouldBe expectedResponse
        }
    }

    @Test
    fun `getChatSummary when chat does not exists`() {
        val (user1) = generateLongs(1)
        val request = ru.zveron.contract.chat.getChatSummary {
            chatId = Uuids.timeBased().toString()
        }

        shouldThrow<InvalidParamChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                chatApplicationService.getChatSummary(request, defaultContext())
            }
        }.message shouldStartWith "Profile with id $user1 does not have chat with id: ${request.chatId}"
    }

    @Test
    fun startChat() {
        val (user1, user2, lot1) = generateLongs(3)
        val message = generateString(30)
        val request = startChatRequest {
            interlocutorId = user2
            article = article {
                id = lot1
                type = ArticleType.LOT
            }
            text = message
        }
        val profile2 = generateProfile(user2)
        val lot = generateLot(lot1)

        coEvery {
            profileClient.getProfilesSummary(listOf(user2))
        } returns listOf(profile2)
        coEvery {
            lotClient.getLotsById(listOf(lot1))
        } returns listOf(lot)
        coEvery {
            blacklistClient.existsInBlacklist(user2, user1)
        } returns false


        runBlocking(MetadataElement(Metadata(user1))) {
            chatApplicationService.startChat(request, defaultContext())
                .responses[user1]!!.chatSummary.chat.newChatShouldBe(profile2.toChatSummary(), lot, message, user1)
        }
    }

    @Test
    fun `startChat when start chat with yourself`() {
        val (user1) = generateLongs(1)
        val request = startChatRequest {
            interlocutorId = user1
        }

        assertThrows<InvalidParamChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                chatApplicationService.startChat(request, defaultContext())
            }
        }.message shouldStartWith "Cannot start chat with yourself"
    }

    @Test
    fun `startChat when in blacklist`() {
        val (user1, user2, lot1) = generateLongs(3)
        val message = generateString(30)
        val request = startChatRequest {
            interlocutorId = user2
            article = article {
                id = lot1
                type = ArticleType.LOT
            }
            text = message
        }

        coEvery {
            blacklistClient.existsInBlacklist(user2, user1)
        } returns true


        assertThrows<InvalidParamChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                chatApplicationService.startChat(request, defaultContext())
            }
        }.message shouldStartWith "Cannot start chat with profile: $user2 because you are in the blacklist."
    }

    @Test
    fun `sendEvent when changed status event`() {
        val (msg1, msg2, msg3) = PrimitivesGenerator.generateNTimeUuids(3)
        val (user1, user2) = generateLongs(5)
        val chat1 = ChatGenerator.generateChat(user1, user2)
        val message1 = generateMessage(chat1.chatId, msg1, user2)
        val message2 = generateMessage(chat1.chatId, msg2, user2)
        val message3 = generateMessage(chat1.chatId, msg3, user2)
        val request = sendEventRequest {
            chatId = chat1.chatId.toString()
            changedStatusEvent = changeMessagesStatusEvent {
                ids.addAll(listOf(msg1.toString(), msg2.toString()))
                isRead = true
            }
        }

        runBlocking(MetadataElement(Metadata(user1))) {
            chatRepository.save(chat1)
            messageRepository.save(message1)
            messageRepository.save(message2)
            messageRepository.save(message3)

            chatApplicationService.sendEvent(request, defaultContext())

            messageRepository.findByChatIdAndId(chat1.chatId, msg1)!!.isRead shouldBe true
            messageRepository.findByChatIdAndId(chat1.chatId, msg2)!!.isRead shouldBe true
            messageRepository.findByChatIdAndId(chat1.chatId, msg3)!!.isRead shouldBe false
        }
    }

    @Test
    fun `sendEvent when disconnect event`() {
        val (user1, user2) = generateLongs(5)
        val chat1 = ChatGenerator.generateChat(user1, user2)
        val request = sendEventRequest {
            chatId = chat1.chatId.toString()
            disconnectEvent = disconnectEvent
        }

        runBlocking(MetadataElement(Metadata(user1))) {
            chatRepository.save(chat1)

            val event = chatApplicationService.sendEvent(request, defaultContext())

            event.responseBody.receiveEvent.disconnectEvent.lastOnlineFormatted shouldBe "Не в сети"
            event.targetProfileId shouldBe user2
        }
    }

    @Test
    fun `sendEvent when disconnect event when chat does not exists`() {
        val (user1, user2) = generateLongs(5)
        val chat1 = ChatGenerator.generateChat(user1, user2)
        val request = sendEventRequest {
            chatId = chat1.chatId.toString()
            disconnectEvent = disconnectEvent { }
        }

        shouldThrow<InvalidParamChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                chatApplicationService.sendEvent(request, defaultContext())
            }
        }.message shouldStartWith "Chat: ${chat1.chatId} does not exists for profile: $user1"
    }

    @Test
    fun `sendEvent when connect event`() {
        val (user1, user2) = generateLongs(5)
        val chat1 = ChatGenerator.generateChat(user1, user2)
        val request = sendEventRequest {
            chatId = chat1.chatId.toString()
            noPayloadEvent = noPayloadEvent { type = NoPayloadEventType.ONLINE }
        }

        runBlocking(MetadataElement(Metadata(user1))) {
            chatRepository.save(chat1)

            val event = chatApplicationService.sendEvent(request, defaultContext())

            event.responseBody.receiveEvent.noPayloadEvent.type shouldBe NoPayloadEventType.ONLINE
            event.targetProfileId shouldBe user2
        }
    }

    @Test
    fun `sendEvent when texting event`() {
        val (user1, user2) = generateLongs(5)
        val chat1 = ChatGenerator.generateChat(user1, user2)
        val request = sendEventRequest {
            chatId = chat1.chatId.toString()
            noPayloadEvent = noPayloadEvent { type = NoPayloadEventType.TEXTING }
        }

        runBlocking(MetadataElement(Metadata(user1))) {
            chatRepository.save(chat1)

            val event = chatApplicationService.sendEvent(request, defaultContext())

            event.responseBody.receiveEvent.noPayloadEvent.type shouldBe NoPayloadEventType.TEXTING
            event.targetProfileId shouldBe user2
        }
    }

    @Test
    fun `sendEvent when no payload and chat does not exists`() {
        val (user1, user2) = generateLongs(5)
        val chat1 = ChatGenerator.generateChat(user1, user2)
        val request = sendEventRequest {
            chatId = chat1.chatId.toString()
            noPayloadEvent = noPayloadEvent { }
        }

        shouldThrow<InvalidParamChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                chatApplicationService.sendEvent(request, defaultContext())
            }
        }.message shouldStartWith "Chat: ${chat1.chatId} does not exists for profile: $user1"
    }

    @Test
    fun `sendEvent when invalid event case`() {
        val (user1, user2) = generateLongs(5)
        val chat1 = ChatGenerator.generateChat(user1, user2)
        val request = sendEventRequest {
            chatId = chat1.chatId.toString()
        }

        shouldThrow<InvalidParamChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                chatRepository.save(chat1)
                chatApplicationService.sendEvent(request, defaultContext())
            }
        }.message shouldStartWith "Unsupported event type: ${request.eventCase.name}."
    }

    private fun CoroutineScope.defaultContext() = ChatRequestContext(
        GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!,
    )
}