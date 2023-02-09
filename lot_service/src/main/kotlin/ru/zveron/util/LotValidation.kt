package ru.zveron.util

import io.grpc.Status
import ru.zveron.contract.lot.model.CommunicationChannel
import ru.zveron.contract.lot.model.Photo
import ru.zveron.exception.LotException
import ru.zveron.model.SellerProfile

object LotValidation {

    const val DEFAULT_ID_FOR_PHOTO = 1L

    fun SellerProfile.validateContacts(communicationChannel: List<CommunicationChannel>) {
        if (communicationChannel.size !in 1..2) {
            throw LotException(Status.INVALID_ARGUMENT, "Communication channel should be taken between 1 and 2")
        }

        for (channel in communicationChannel) {
            val exists = when (channel) {
                CommunicationChannel.VK -> contact.vk
                CommunicationChannel.EMAIL -> contact.email
                CommunicationChannel.PHONE -> contact.phone
                CommunicationChannel.CHAT -> true
                else -> false
            }

            if (!exists) {
                throw LotException(
                    Status.INVALID_ARGUMENT,
                    "User selected communication channel, which doesnt have for him"
                )
            }
        }
    }

    fun List<Photo>.validate() {
        if (isEmpty()) {
            throw LotException(Status.INVALID_ARGUMENT, "Necessary more than zero photo must be specified for lot")
        }

        val uniqueId = mutableSetOf<Long>()
        for ((index, photo) in this.withIndex()) {
            uniqueId.add(photo.id)
            // Проверяем, что порядок фотографий всегда начинается с 0 и увеличивается константно на +1. К примеру: 1, 2, 3
            if (index != photo.order) {
                throw LotException(Status.INVALID_ARGUMENT, "Order photos should sequential growth by 1: 0, 1, 2...")
            }

            if (photo.id == DEFAULT_ID_FOR_PHOTO) {
                throw LotException(Status.INVALID_ARGUMENT, "None of the photos should be a default photo")
            }
        }

        if (uniqueId.size != this.size) {
            throw LotException(Status.INVALID_ARGUMENT, "User cant select same photos several time")
        }
    }
}