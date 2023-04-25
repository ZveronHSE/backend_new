package ru.zveron.mapper

import org.junit.jupiter.api.Test
import ru.zveron.common.assertion.MessageAssertions.messageShouldBe
import ru.zveron.common.generator.MessageGenerator.generateMessage
import ru.zveron.common.generator.PrimitivesGenerator.generateLong
import ru.zveron.common.generator.PrimitivesGenerator.generateNTimeUuids
import ru.zveron.common.generator.PrimitivesGenerator.generateString
import ru.zveron.contract.chat.model.message
import ru.zveron.mapper.ProtoTypesMapper.toTimestamp

class MessageMapperTest {

    @Test
    fun messageToResponse() {
        val lotId = generateLong()
        val (profileId, chatId) = generateNTimeUuids(2)
        val message = generateMessage(chatId, profileId, lotId, imagesUrls = listOf(generateString(10), generateString(10)))
        val expectedMessage = message {
            id = message.id.toString()
            text = message.text
            isRead = message.isRead
            message.imagesUrls?.apply { imagesUrls.addAll(this) }
            senderId = message.senderId
            sentAt = message.receivedAt.toTimestamp()
        }

        MessageMapper.messageToResponse(message) messageShouldBe expectedMessage
    }
}