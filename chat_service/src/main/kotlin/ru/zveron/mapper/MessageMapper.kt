package ru.zveron.mapper

import ru.zveron.contract.chat.model.message
import ru.zveron.mapper.ProtoTypesMapper.toTimestamp
import ru.zveron.model.entity.Message

object MessageMapper {

    fun messageToResponse(message: Message): ru.zveron.contract.chat.model.Message =
        message {
            id = message.id.toString()
            text = message.text
            isRead = message.isRead
            senderId = message.senderId
            message.imagesUrls?.let { imagesUrls.addAll(it) }
            sentAt = message.receivedAt.toTimestamp()
        }
}