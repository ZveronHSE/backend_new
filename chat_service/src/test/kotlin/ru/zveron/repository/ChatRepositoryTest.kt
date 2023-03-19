package ru.zveron.repository

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.ChatServiceApplicationTest
import ru.zveron.model.entity.Chat
import ru.zveron.model.enum.ChatStatus
import ru.zveron.model.enum.FolderType
import java.time.Instant
import java.util.*

class ChatRepositoryTest : ChatServiceApplicationTest() {

    @Autowired
    lateinit var chatRepository: ChatRepository

    @Test
    fun `Correct data write and read`() {
        val chat =
            Chat(1, FolderType.DEFAULT, UUID.randomUUID(), Instant.now(), 2, null, null,
                2, ChatStatus.DEFAULT, null)
        runBlocking {
            chatRepository.save(chat)
            chatRepository.findAll().count() shouldBe 1
        }
    }
}