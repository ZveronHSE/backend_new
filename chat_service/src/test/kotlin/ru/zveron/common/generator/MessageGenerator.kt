package ru.zveron.common.generator

import ru.zveron.common.generator.PrimitivesGenerator.generateString
import ru.zveron.mapper.ProtoTypesMapper.toTimestamp
import ru.zveron.model.entity.Message
import ru.zveron.model.constant.MessageType
import java.time.Instant
import java.util.UUID

object MessageGenerator {

    fun generateMessage(
        chatId: UUID,
        id: UUID,
        sender: Long,
        receivedAt: Instant = Instant.now(),
        isRead: Boolean = false,
        imagesUrls: List<String>? = null,
    ) = Message(
        chatId = chatId,
        id = id,
        receivedAt = receivedAt,
        senderId = sender,
        text = generateString(100),
        isRead = isRead,
        imagesUrls = imagesUrls,
        type = MessageType.DEFAULT,
    )

    fun generateMessageResponse(message: Message) = ru.zveron.contract.chat.model.message {
        id = message.id.toString()
        text = message.text
        isRead = message.isRead
        message.imagesUrls?.apply { imagesUrls.addAll(this) }
        senderId = message.senderId
        sentAt = message.receivedAt.toTimestamp()
    }
}