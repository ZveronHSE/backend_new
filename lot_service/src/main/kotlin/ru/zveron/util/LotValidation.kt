package ru.zveron.util

import io.grpc.Status
import ru.zveron.contract.lot.model.CommunicationChannel
import ru.zveron.contract.lot.model.Photo
import ru.zveron.exception.LotException
import ru.zveron.model.SellerProfile

object LotValidation {

    const val DEFAULT_URL_FOR_PHOTO = """https://storage.yandexcloud.net/zveron-profile/random.jpeg"""

    fun SellerProfile.validateContacts(communicationChannel: List<CommunicationChannel>) {
        if (communicationChannel.size !in 1..2) {
            throw LotException(Status.INVALID_ARGUMENT, "Communication channel should be taken between 1 and 2")
        }

        for (channel in communicationChannel) {
            val exists = when (channel) {
                CommunicationChannel.VK -> contact.isVk
                CommunicationChannel.EMAIL -> contact.isEmail
                CommunicationChannel.PHONE -> contact.isPhone
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

        val uniqueId = mutableSetOf<String>()
        for ((index, photo) in this.withIndex()) {
            uniqueId.add(photo.url)
            // Проверяем, что порядок фотографий всегда начинается с 0 и увеличивается константно на +1. К примеру: 1, 2, 3
            if (index != photo.order) {
                throw LotException(Status.INVALID_ARGUMENT, "Order photos should sequential growth by 1: 0, 1, 2...")
            }

            if (photo.url == DEFAULT_URL_FOR_PHOTO) {
                throw LotException(Status.INVALID_ARGUMENT, "None of the photos should be a default photo")
            }
        }

        if (uniqueId.size != this.size) {
            throw LotException(Status.INVALID_ARGUMENT, "User cant select same photos several time")
        }
    }
}