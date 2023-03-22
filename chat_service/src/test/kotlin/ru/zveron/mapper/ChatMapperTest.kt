package ru.zveron.mapper

import org.junit.jupiter.api.Test
import ru.zveron.common.assertion.ChatAssertions.chatShouldBe
import ru.zveron.common.assertion.ChatAssertions.profileShouldBe
import ru.zveron.common.generator.ChatGenerator
import ru.zveron.common.generator.LotGenerator
import ru.zveron.common.generator.MessageGenerator
import ru.zveron.common.generator.PrimitivesGenerator
import ru.zveron.common.generator.ProfileSummaryGenerator
import ru.zveron.contract.chat.model.ChatFolder
import ru.zveron.contract.chat.model.chat
import ru.zveron.contract.chat.model.profileSummary
import ru.zveron.mapper.ChatMapper.toChatSummary
import ru.zveron.mapper.MessageMapper.messageToResponse
import ru.zveron.mapper.ProtoTypesMapper.toTimestamp
import java.time.Instant

class ChatMapperTest {

    @Test
    fun chatToChatResponse() {
        val timestamp = Instant.now()
        val (msg1, msg2) = PrimitivesGenerator.generateNTimeUuids(2)
        val (user1, user2, lot1, serviceId, reviewId) = PrimitivesGenerator.generateLongs(5)
        val chat1 = ChatGenerator.generateChat(
            user1,
            user2,
            lastUpdate = timestamp,
            serviceId = serviceId,
            reviewId = reviewId
        )
        val profile2 = ProfileSummaryGenerator.generateProfile(user2)
        val lot = LotGenerator.generateLot(lot1)
        val message1 = messageToResponse(MessageGenerator.generateMessage(chat1.chatId, msg1, user2))
        val message2 = messageToResponse(MessageGenerator.generateMessage(chat1.chatId, msg2, user1))
        val expectedChat = chat {
            chatId = chat1.chatId.toString()
            interlocutorSummary = profile2.toChatSummary()
            messages.addAll(listOf(message1, message2))
            lastUpdate = timestamp.toTimestamp()
            this.serviceId = serviceId
            this.reviewId = reviewId
            lots.add(lot)
            folder = ChatFolder.NONE
            isBlocked = true
        }

        val actualChat = ChatMapper.chatToChatResponse(
            chat1,
            profile2.toChatSummary(),
            listOf(lot),
            listOf(message1, message2),
            true
        )

        actualChat chatShouldBe expectedChat
    }

    @Test
    fun toChatSummary() {
        val user1 = PrimitivesGenerator.generateLong()
        val profile2 = ProfileSummaryGenerator.generateProfile(user1)
        val expectedProfile = profileSummary {
            id = profile2.id
            imageUrl = profile2.imageUrl
            name = profile2.name
            surname = profile2.surname
            isOnline = false
            formattedOnlineStatus = "Не в сети"
        }

        val actualProfile = profile2.toChatSummary()

        actualProfile profileShouldBe expectedProfile
    }
}