package ru.zveron.repository

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.ChatServiceApplicationTest
import ru.zveron.common.generator.ChatGenerator
import ru.zveron.common.generator.MessageGenerator
import ru.zveron.common.generator.PrimitivesGenerator

class BatchChatRepositoryTest: ChatServiceApplicationTest() {

    @Autowired
    lateinit var messageRepository: MessageRepository
    @Autowired
    lateinit var batchChatRepository: BatchChatRepository

    @Test
    fun `markMessagesAsRead and messages statuses are changed`() {
        val (msg1, msg2, msg3) = PrimitivesGenerator.generateNTimeUuids(3)
        val (user1, user2) = PrimitivesGenerator.generateLongs(5)
        val chat1 = ChatGenerator.generateChat(user1, user2)
        val message1 = MessageGenerator.generateMessage(chat1.chatId, msg1, user2)
        val message2 = MessageGenerator.generateMessage(chat1.chatId, msg2, user2)
        val message3 = MessageGenerator.generateMessage(chat1.chatId, msg3, user2)

        runBlocking {
            messageRepository.save(message1)
            messageRepository.save(message2)
            messageRepository.save(message3)

            batchChatRepository.markMessagesAsRead(chat1.chatId, listOf(msg1, msg2, msg3))

            messageRepository.findByChatIdAndId(chat1.chatId, msg1)!!.isRead shouldBe true
            messageRepository.findByChatIdAndId(chat1.chatId, msg2)!!.isRead shouldBe true
            messageRepository.findByChatIdAndId(chat1.chatId, msg3)!!.isRead shouldBe true
        }
    }
}