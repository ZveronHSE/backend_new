package ru.zveron.service.application

import com.ninjasquad.springmockk.MockkBean
import io.grpc.Status
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.mockk.coEvery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.ChatServiceApplicationTest
import ru.zveron.client.blacklist.BlacklistClient
import ru.zveron.common.assertion.MessageAssertions.responseShouldBe
import ru.zveron.common.generator.ChatGenerator
import ru.zveron.common.generator.MessageGenerator
import ru.zveron.common.generator.MessageGenerator.generateMessageResponse
import ru.zveron.common.generator.PrimitivesGenerator
import ru.zveron.common.generator.PrimitivesGenerator.generateString
import ru.zveron.contract.chat.getChatMessagesRequest
import ru.zveron.contract.chat.getChatMessagesResponse
import ru.zveron.contract.chat.model.MessageType
import ru.zveron.contract.chat.model.messagePagination
import ru.zveron.contract.chat.sendMessageRequest
import ru.zveron.exception.ChatException
import ru.zveron.exception.InvalidParamChatException
import ru.zveron.library.grpc.interceptor.model.MetadataElement
import ru.zveron.library.grpc.model.Metadata
import ru.zveron.library.grpc.util.GrpcUtils
import ru.zveron.model.dao.ChatRequestContext
import ru.zveron.repository.ChatRepository
import ru.zveron.repository.MessageRepository
import java.time.Instant
import java.util.*

class MessageApplicationServiceTest : ChatServiceApplicationTest() {
    @Autowired
    lateinit var chatRepository: ChatRepository

    @Autowired
    lateinit var messageRepository: MessageRepository

    @Autowired
    lateinit var messageApplicationService: MessageApplicationService

    @MockkBean
    lateinit var blacklistClient: BlacklistClient

    @Test
    fun getRecentMessagesByChat() {
        val (msg1, msg2, msg3) = PrimitivesGenerator.generateNTimeUuids(3)
        val (user1, user2) = PrimitivesGenerator.generateLongs(2)
        val chat1 = ChatGenerator.generateChat(user1, user2)
        val message1 = MessageGenerator.generateMessage(chat1.chatId, msg1, user2)
        val message2 = MessageGenerator.generateMessage(chat1.chatId, msg2, user1)
        val message3 = MessageGenerator.generateMessage(chat1.chatId, msg3, user2)
        val request = getChatMessagesRequest {
            chatId = chat1.chatId.toString()
        }
        val expectedResponse = getChatMessagesResponse {
            messages.add(generateMessageResponse(message3))
            messages.add(generateMessageResponse(message2))
            messages.add(generateMessageResponse(message1))
        }

        runBlocking(MetadataElement(Metadata(user1))) {
            messageRepository.save(message1)
            messageRepository.save(message2)
            messageRepository.save(message3)
            chatRepository.save(chat1)

            messageApplicationService.getRecentMessagesByChat(
                request,
                defaultContext()
            ).responseBody.getMessagesResponse responseShouldBe expectedResponse
        }
    }

    @Test
    fun `getRecentMessagesByChat with pagination`() {
        val timestamp = Instant.now()
        val (msg1, msg2, msg3, msg4) = PrimitivesGenerator.generateNTimeUuids(4)
        val (user1, user2) = PrimitivesGenerator.generateLongs(2)
        val chat1 = ChatGenerator.generateChat(user1, user2)
        val message1 =
            MessageGenerator.generateMessage(chat1.chatId, msg1, user2, receivedAt = timestamp.minusSeconds(3))
        val message2 =
            MessageGenerator.generateMessage(chat1.chatId, msg2, user1, receivedAt = timestamp.minusSeconds(2))
        val message3 =
            MessageGenerator.generateMessage(chat1.chatId, msg3, user2, receivedAt = timestamp.minusSeconds(1))
        val message4 = MessageGenerator.generateMessage(chat1.chatId, msg4, user2, receivedAt = timestamp)
        val request = getChatMessagesRequest {
            chatId = chat1.chatId.toString()
            pagination = messagePagination {
                size = 2
                messagesBeforeId = msg4.toString()
            }
        }
        val expectedResponse = getChatMessagesResponse {
            messages.add(generateMessageResponse(message3))
            messages.add(generateMessageResponse(message2))
        }

        runBlocking(MetadataElement(Metadata(user1))) {
            messageRepository.save(message1)
            messageRepository.save(message2)
            messageRepository.save(message3)
            messageRepository.save(message4)
            chatRepository.save(chat1)

            messageApplicationService.getRecentMessagesByChat(
                request,
                defaultContext()
            ).responseBody.getMessagesResponse responseShouldBe expectedResponse
        }
    }

    @Test
    fun `getRecentMessagesByChat when chat does not exist`() {
        val (user1, user2) = PrimitivesGenerator.generateLongs(2)
        val chat1 = ChatGenerator.generateChat(user1, user2)
        val request = getChatMessagesRequest {
            chatId = chat1.chatId.toString()
        }

        val exception = shouldThrow<ChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                messageApplicationService.getRecentMessagesByChat(request, defaultContext())
            }
        }

        exception.status shouldBe Status.NOT_FOUND
        exception.message shouldStartWith "Profile: $user1 does not have chat: ${chat1.chatId}."
    }

    @Test
    fun sendMessage() {
        val (msg1) = PrimitivesGenerator.generateNTimeUuids(1)
        val (user1, user2) = PrimitivesGenerator.generateLongs(2)
        val chat1 = ChatGenerator.generateChat(user1, user2)
        val message1 = MessageGenerator.generateMessage(chat1.chatId, msg1, user2)
        val text = generateString(30)
        val image1 = generateString(20)
        val image2 = generateString(20)
        val request = sendMessageRequest {
            chatId = chat1.chatId.toString()
            type = MessageType.DEFAULT
            this.text = text
            imagesUrls.add(image1)
            imagesUrls.add(image2)
        }

        coEvery { blacklistClient.existsInBlacklist(user2, user1) } returns false

        runBlocking(MetadataElement(Metadata(user1))) {
            messageRepository.save(message1)
            chatRepository.save(chat1)

            messageApplicationService.sendMessage(request, defaultContext())

            messageRepository.findAll().first().apply {
                chatId shouldBe chat1.chatId
                this.text shouldBe text
                imagesUrls shouldContainExactly listOf(image1, image2)
                senderId shouldBe user1
                isRead shouldBe false
                type shouldBe ru.zveron.model.constant.MessageType.DEFAULT
            }
        }
    }

    @Test
    fun `sendMessage when chat does not exist`() {
        val (user1, user2) = PrimitivesGenerator.generateLongs(2)
        val chat1 = ChatGenerator.generateChat(user1, user2)
        val request = sendMessageRequest {
            chatId = chat1.chatId.toString()
        }

        val exception = shouldThrow<ChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                messageApplicationService.sendMessage(request, defaultContext())
            }
        }

        exception.status shouldBe Status.NOT_FOUND
        exception.message shouldStartWith "Profile: $user1 does not have chat: ${chat1.chatId}."
    }

    @Test
    fun `sendMessage when user in blacklist`() {
        val (user1, user2) = PrimitivesGenerator.generateLongs(2)
        val chat1 = ChatGenerator.generateChat(user1, user2)
        val request = sendMessageRequest {
            chatId = chat1.chatId.toString()
        }

        coEvery { blacklistClient.existsInBlacklist(user2, user1) } returns true

        val exception = shouldThrow<InvalidParamChatException> {
            runBlocking(MetadataElement(Metadata(user1))) {
                chatRepository.save(chat1)

                messageApplicationService.sendMessage(request, defaultContext())
            }
        }

        exception.message shouldStartWith "Cannot send message because authorized profile is in the blacklist of profile $user2."
    }

    private fun CoroutineScope.defaultContext() = ChatRequestContext(
        UUID.randomUUID(),
        GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!,
    )
}