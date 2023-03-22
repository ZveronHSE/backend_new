package ru.zveron.repository

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.ChatServiceApplicationTest
import ru.zveron.common.assertion.MessageAssertions.messageShouldBe
import ru.zveron.common.generator.MessageGenerator.generateMessage
import ru.zveron.common.generator.PrimitivesGenerator.generateLongs
import ru.zveron.common.generator.PrimitivesGenerator.generateNTimeUuids
import ru.zveron.common.generator.PrimitivesGenerator.generateNUuids

class MessageRepositoryTest : ChatServiceApplicationTest() {

    @Autowired
    private lateinit var messageRepository: MessageRepository

    @Test
    fun getChatRecentMessages() {
        val (chatId1, chatId2, chatId3) = generateNUuids(3)
        val (msg1, msg2, msg3, msg4, msg5) = generateNTimeUuids(5)
        val (sender1, sender2, sender3) = generateLongs(3)
        val targetMessage1 = generateMessage(chatId1, msg1, sender1)
        val targetMessage2 = generateMessage(chatId1, msg2, sender1)
        val targetMessage3 = generateMessage(chatId1, msg3, sender1)

        runBlocking {
            messageRepository.save(targetMessage1)
            messageRepository.save(targetMessage2)
            messageRepository.save(targetMessage3)
            messageRepository.save(generateMessage(chatId2, msg4, sender2))
            messageRepository.save(generateMessage(chatId3, msg5, sender3))

            messageRepository.getChatRecentMessages(chatId1, 2).toList().apply {
                size shouldBe 2
                first() messageShouldBe targetMessage3
                component2() messageShouldBe targetMessage2
            }
        }
    }

    @Test
    fun `getChatRecentMessages with pagination`() {
        val (chatId1, chatId2, chatId3) = generateNUuids(3)
        val (msg1, msg2, msg3, msg4, msg5) = generateNTimeUuids(5)
        val (sender1, sender2, sender3) = generateLongs(3)
        val targetMessage1 = generateMessage(chatId1, msg1, sender1)
        val targetMessage2 = generateMessage(chatId1, msg2, sender1)
        val targetMessage3 = generateMessage(chatId1, msg3, sender1)

        runBlocking {
            messageRepository.save(targetMessage1)
            messageRepository.save(targetMessage2)
            messageRepository.save(targetMessage3)
            messageRepository.save(generateMessage(chatId2, msg4, sender2))
            messageRepository.save(generateMessage(chatId3, msg5, sender3))

            messageRepository.getChatRecentMessages(chatId1, msg3, 2).toList().apply {
                size shouldBe 2
                first() messageShouldBe targetMessage2
                component2() messageShouldBe targetMessage1
            }
        }
    }
}