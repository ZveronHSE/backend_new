package ru.zveron.test.util.model

import ru.zveron.contract.lot.createLotRequest
import ru.zveron.contract.lot.fullAddress
import ru.zveron.contract.lot.model.CommunicationChannel
import ru.zveron.contract.lot.model.photo
import ru.zveron.entity.Lot
import ru.zveron.entity.LotStatistics
import ru.zveron.model.ChannelType
import ru.zveron.model.enum.Gender
import ru.zveron.model.enum.LotStatus
import java.time.Instant

object LotEntities {

    fun mockLotEntity(
        sellerId: Long = 1L,
        createdAt: Instant = Instant.now()
    ) = Lot(
        title = "title",
        description = "description",
        price = 3,
        lotFormId = 1,
        createdAt = createdAt,
        status = LotStatus.ACTIVE,
        gender = Gender.MALE,
        sellerId = sellerId,
        channelType = ChannelType(isChat = true),
        addressId = 1L,
        categoryId = 1
    )

    fun mockLotStatistics(lot: Lot) = LotStatistics(lot = lot)

    fun mockCreateLot() = createLotRequest {
        title = "title"
        photos.addAll(listOf(
            photo {
                id = 3
                order = 0
            }, photo {
                id = 2
                order = 1
            })
        )
        parameters.putAll(mapOf(1 to "1", 2 to "2"))
        description = "description"
        price = 5
        communicationChannel.add(CommunicationChannel.CHAT)
        gender = ru.zveron.contract.lot.model.Gender.MALE
        address = fullAddress {
            town = "Москва"
            street = "Покровский бульвар"
            house = "11"
            latitude = 12.0
            longitude = 23.0
        }
        lotFormId = 1
        categoryId = 3
    }
}