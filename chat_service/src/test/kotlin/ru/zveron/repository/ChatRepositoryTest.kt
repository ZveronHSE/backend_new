package ru.zveron.repository

import com.datastax.oss.driver.api.core.uuid.Uuids
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.ChatServiceApplicationTest
import ru.zveron.common.assertion.ChatAssertions.chatShouldBe
import ru.zveron.common.generator.ChatGenerator.generateChat
import ru.zveron.common.generator.PrimitivesGenerator.generateLong
import ru.zveron.common.generator.PrimitivesGenerator.generateLongs
import ru.zveron.common.generator.PrimitivesGenerator.generateString
import ru.zveron.model.constant.ChatStatus
import ru.zveron.model.constant.MessageType
import java.time.Instant

class ChatRepositoryTest : ChatServiceApplicationTest() {

    @Autowired
    private lateinit var chatRepository: ChatRepository

    @Autowired
    lateinit var messageRepository: MessageRepository

    @Test
    fun getInterlocutorId() {
        val (user1, user2, user3) = generateLongs(3)
        val chat1 = generateChat(user1, user2)
        val chat2 = generateChat(user1, user3, lotsIds = setOf(1))
        val chat3 = generateChat(user3, user2, lotsIds = setOf(2, 3))

        runBlocking {
            chatRepository.save(chat1)
            chatRepository.save(chat2)
            chatRepository.save(chat3)

            chatRepository.getInterlocutorId(user1, chat1.chatId) shouldBe user2
        }
    }

    @Test
    fun `getInterlocutorId if chat does not exists`() {
        val (user1, user2, user3) = generateLongs(3)
        val chat1 = generateChat(user1, user3, lotsIds = setOf(1))
        val chat2 = generateChat(user3, user2, lotsIds = setOf(2, 3))

        runBlocking {
            chatRepository.save(chat1)
            chatRepository.save(chat2)

            chatRepository.getInterlocutorId(user1, Uuids.timeBased()) shouldBe null
        }
    }

    @Test
    fun getChats() {
        val (user1, user2, user3) = generateLongs(3)
        val chat1 = generateChat(user1, user2)
        val chat2 = generateChat(user1, user3, lotsIds = setOf(1))
        val chat3 = generateChat(user3, user2, lotsIds = setOf(2, 3))

        runBlocking {
            chatRepository.save(chat1)
            chatRepository.save(chat2)
            chatRepository.save(chat3)

            chatRepository.findAllByProfileId(user1).toList().apply {
                first() chatShouldBe chat2
                component2() chatShouldBe chat1
            }
        }
    }

    @Test
    fun `getChats with pagination`() {
        val lastUpdate = Instant.now()
        val (user1, user2, user3) = generateLongs(3)
        val chat1 = generateChat(user1, user2, lastUpdate = lastUpdate.minusSeconds(1))
        val chat2 = generateChat(user1, user3, lotsIds = setOf(1), lastUpdate = lastUpdate)
        val chat3 = generateChat(user3, user2, lotsIds = setOf(2, 3))

        runBlocking {
            chatRepository.save(chat1)
            chatRepository.save(chat2)
            chatRepository.save(chat3)

            chatRepository.findAllByProfileIdBeforeTimestamp(user1, lastUpdate).first() chatShouldBe chat1
        }
    }

    @Test
    fun `getChats if invalid userId`() {
        val user1 = generateLong()
        runBlocking {
            chatRepository.findAllByProfileId(user1).count() shouldBe 0
        }
    }

    @Test
    fun attachLot() {
        val (user1, user2, user3, lot1, lot2) = generateLongs(5)
        val chat1 = generateChat(user1, user2)
        val chat2 = generateChat(user1, user3, lotsIds = setOf(lot1))
        val chat2Reversed = chat2.copy(profileId = user3, anotherProfileId = user1)
        val chat3 = generateChat(user3, user2)

        runBlocking {
            chatRepository.save(chat1)
            chatRepository.save(chat2)
            chatRepository.save(chat2Reversed)
            chatRepository.save(chat3)

            chatRepository.attachLot(lot2, user1, user3, chat2.chatId)

            chatRepository.findExact(user1, chat2.chatId)?.lotsIds shouldContainExactlyInAnyOrder listOf(lot1, lot2)
            chatRepository.findExact(user3, chat2.chatId)?.lotsIds shouldContainExactlyInAnyOrder listOf(lot1, lot2)
        }
    }

    @Test
    fun `attachLot when invalid ids`() {
        val (user1, user2, user3, lot1, lot2) = generateLongs(5)
        val chat1 = generateChat(user1, user2)
        val chat2 = generateChat(user1, user3, lotsIds = setOf(lot1))
        val chat3 = generateChat(user3, user2)

        shouldNotThrow<RuntimeException> {
            runBlocking {
                chatRepository.save(chat1)
                chatRepository.save(chat2)
                chatRepository.save(chat3)

                chatRepository.attachLot(lot2, user1, user2, Uuids.timeBased())

                chatRepository.findExact(user1, chat1.chatId)?.lotsIds shouldBe null
                chatRepository.findExact(user1, chat2.chatId)?.lotsIds shouldContainExactlyInAnyOrder listOf(lot1)

            }
        }
    }

    @Test
    fun detachLot() {
        val (user1, user2, user3, lot1) = generateLongs(4)
        val chat1 = generateChat(user1, user2)
        val chat2 = generateChat(user1, user3, lotsIds = setOf(lot1))
        val chat2Reversed = chat2.copy(profileId = user3, anotherProfileId = user1)
        val chat3 = generateChat(user3, user2)

        runBlocking {
            chatRepository.save(chat1)
            chatRepository.save(chat2)
            chatRepository.save(chat2Reversed)
            chatRepository.save(chat3)

            chatRepository.detachLot(lot1, user1, user2, chat2.chatId)

            chatRepository.findExact(user1, chat2.chatId)?.lotsIds shouldBe null
            chatRepository.findExact(user2, chat2.chatId)?.lotsIds shouldBe null
        }
    }

    @Test
    fun `detachLot when invalid ids`() {
        val (user1, user2, user3, lot1) = generateLongs(4)
        val chat1 = generateChat(user1, user2)
        val chat2 = generateChat(user1, user3, lotsIds = setOf(lot1))
        val chat3 = generateChat(user3, user2)

        shouldNotThrow<RuntimeException> {
            runBlocking {
                chatRepository.save(chat1)
                chatRepository.save(chat2)
                chatRepository.save(chat3)

                chatRepository.detachLot(lot1, user1, user2, Uuids.timeBased())

                chatRepository.findExact(user1, chat2.chatId)?.lotsIds shouldBe listOf(lot1)
            }
        }
    }

    @Test
    fun crateChatsPair() {
        val timestamp = Instant.now()
        val chatId = Uuids.timeBased()
        val messageId = Uuids.timeBased()
        val text = generateString(50)
        val (user1, user2, lot1) = generateLongs(3)
        runBlocking {
            chatRepository.crateChatsPair(
                user1,
                chatId,
                timestamp,
                user2,
                ChatStatus.DEFAULT,
                messageId,
                text,
                MessageType.DEFAULT,
                lot1,
            )

            chatRepository.findExact(user1, chatId) shouldNotBe null
            chatRepository.findExact(user2, chatId) shouldNotBe null
            messageRepository.findExact(chatId, messageId) shouldNotBe null
        }
    }
}