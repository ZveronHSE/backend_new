package ru.zveron.test.util.model

import ru.zveron.entity.Lot
import ru.zveron.entity.LotStatistics
import ru.zveron.model.ChannelType
import ru.zveron.model.constant.Gender
import ru.zveron.model.constant.LotStatus
import java.time.Instant

object LotEntities {

    fun mockLotEntity(
        sellerId: Long = 1L,
        dateCreation: Instant = Instant.now()
    ) = Lot(
        title = "title",
        description = "description",
        price = 3,
        lotFormId = 1,
        dateCreation = dateCreation,
        status = LotStatus.ACTIVE,
        gender = Gender.MALE,
        sellerId = sellerId,
        channelType = ChannelType(chat = true),
        addressId = 1L,
        categoryId = 1
    )

    fun mockLotStatistics(lot: Lot) = LotStatistics(lot = lot)
}