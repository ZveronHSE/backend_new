package ru.zveron.service.presentation

import app.cash.turbine.testIn
import com.datastax.oss.driver.api.core.uuid.Uuids
import io.grpc.Status
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.mockk.coEvery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.ChatServiceApplicationTest
import ru.zveron.common.assertion.ChatAssertions.newChatShouldBe
import ru.zveron.common.assertion.MessageAssertions.messageShouldBe
import ru.zveron.common.generator.LotGenerator
import ru.zveron.common.generator.MessageGenerator.generateMessage
import ru.zveron.common.generator.PrimitivesGenerator
import ru.zveron.common.generator.ProfileSummaryGenerator
import ru.zveron.contract.chat.ArticleType
import ru.zveron.contract.chat.ChatRouteRequest
import ru.zveron.contract.chat.ChatRouteResponse
import ru.zveron.contract.chat.article
import ru.zveron.contract.chat.chatRouteRequest
import ru.zveron.contract.chat.chatRouteResponse
import ru.zveron.contract.chat.model.MessageType
import ru.zveron.contract.chat.receiveMessage
import ru.zveron.contract.chat.sendMessageRequest
import ru.zveron.contract.chat.startChatRequest
import ru.zveron.library.grpc.exception.PlatformException
import ru.zveron.library.grpc.interceptor.model.MetadataElement
import ru.zveron.library.grpc.model.Metadata
import ru.zveron.mapper.ChatMapper.toChatSummary
import ru.zveron.mapper.MessageMapper.messageToResponse
import ru.zveron.repository.ChatRepository
import ru.zveron.repository.MessageRepository
import ru.zveron.component.ChatPersistence
import ru.zveron.model.dao.ChatRequestContext

class ChatServiceExternalTest : ChatServiceApplicationTest() {

    @Autowired
    lateinit var chatRepository: ChatRepository

    @Autowired
    lateinit var messageRepository: MessageRepository

    @Autowired
    lateinit var chatServiceExternal: ChatServiceExternal

    @Autowired
    lateinit var chatPersistence: ChatPersistence

    @Test
    fun bidiChatRoute() {
        val (user1, user2, lot1) = PrimitivesGenerator.generateLongs(3)
        val profile2 = ProfileSummaryGenerator.generateProfile(user2)
        val lot = LotGenerator.generateLot(lot1)
        val message1 = PrimitivesGenerator.generateString(30)
        val message2 = PrimitivesGenerator.generateString(30)
        val backgroundScope = CoroutineScope(MetadataElement(Metadata(user1)))
        val dummyContext = ChatRequestContext(Uuids.timeBased(), user2)

        coEvery {
            profileClient.getProfilesSummary(listOf(user2))
        } returns listOf(profile2)
        coEvery {
            lotClient.getLotsById(listOf(lot1))
        } returns listOf(lot)
        coEvery { blacklistClient.existsInBlacklist(user2, user1) } returns false

        runBlocking(MetadataElement(Metadata(user1))) {
            val inputFlow = Channel<ChatRouteRequest>(2)
            val responseFlow = chatServiceExternal.bidiChatRoute(inputFlow.consumeAsFlow())
            val testedFlow = responseFlow.testIn(backgroundScope)

            inputFlow.send(chatRouteRequest {
                startChat = startChatRequest {
                    interlocutorId = user2
                    article = article {
                        id = lot1
                        type = ArticleType.LOT
                    }
                    text = message1
                }
            })
            testedFlow.awaitItem().apply {
                this.responseCase shouldBe ChatRouteResponse.ResponseCase.CHATSUMMARY
                this.chatSummary.chat.newChatShouldBe(profile2.toChatSummary(), lot, message1, user1)
            }

            val chat1 = chatRepository.findAllByProfileId(user1).first()
            inputFlow.send(chatRouteRequest {
                sendMessage = sendMessageRequest {
                    chatId = chat1.chatId.toString()
                    type = MessageType.DEFAULT
                    this.text = message2
                }
            })
            val message =
                messageToResponse(messageRepository.save(generateMessage(chat1.chatId, Uuids.timeBased(), user2)))
            chatPersistence.sendMessageToConnection(
                user1,
                chatRouteResponse {
                    receiveMessage = receiveMessage {
                        this.message = message
                    }
                },
                dummyContext,
            )
            inputFlow.close()
            testedFlow.awaitItem().apply {
                this.responseCase shouldBe ChatRouteResponse.ResponseCase.RECEIVE_MESSAGE
                this.receiveMessage.message messageShouldBe message
            }


            testedFlow.awaitComplete()
        }
    }

    @Test
    fun `bidiChatRoute do not drop connection on ChatException`() {
        val (user1, lot1) = PrimitivesGenerator.generateLongs(2)
        val message1 = PrimitivesGenerator.generateString(30)
        val message2 = PrimitivesGenerator.generateString(30)
        val backgroundScope = CoroutineScope(MetadataElement(Metadata(user1)))

        runBlocking(MetadataElement(Metadata(user1))) {
            val inputFlow = Channel<ChatRouteRequest>(2)
            val responseFlow = chatServiceExternal.bidiChatRoute(inputFlow.consumeAsFlow())
            val testedFlow = responseFlow.testIn(backgroundScope)

            inputFlow.send(chatRouteRequest {
                startChat = startChatRequest {
                    interlocutorId = user1
                    article = article {
                        id = lot1
                        type = ArticleType.LOT
                    }
                    text = message1
                }
            })
            testedFlow.awaitItem().apply {
                this.responseCase shouldBe ChatRouteResponse.ResponseCase.ERROR
                this.error.apply {
                    status shouldBe Status.INVALID_ARGUMENT.code.value()
                    message shouldStartWith  "Cannot start chat with yourself."
                }
            }

            val fakeChatId = Uuids.timeBased().toString()
            inputFlow.send(chatRouteRequest {
                sendMessage = sendMessageRequest {
                    chatId = fakeChatId
                    type = MessageType.DEFAULT
                    this.text = message2
                }
            })
            testedFlow.awaitItem().apply {
                this.responseCase shouldBe ChatRouteResponse.ResponseCase.ERROR
                this.error.apply {
                    status shouldBe Status.NOT_FOUND.code.value()
                    message shouldStartWith  "Profile: $user1 does not have chat: $fakeChatId."
                }
            }

            inputFlow.close()
            testedFlow.awaitComplete()
        }
    }

    @Test
    fun `bidiChatRoute requires authorization`() {
        val exception = assertThrows<PlatformException> {
            runBlocking {
                val inputFlow = Channel<ChatRouteRequest>(2)
                chatServiceExternal.bidiChatRoute(inputFlow.consumeAsFlow()).collect { }
            }
        }

        exception.status shouldBe Status.UNAUTHENTICATED
        exception.message shouldBe "user should be authorized for this endpoint"
    }
}