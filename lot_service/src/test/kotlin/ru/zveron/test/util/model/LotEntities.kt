package ru.zveron.test.util.model

import ru.zveron.contract.lot.createLotRequest
import ru.zveron.contract.lot.editLotRequest
import ru.zveron.contract.lot.fullAddress
import ru.zveron.contract.lot.model.CommunicationChannel
import ru.zveron.contract.lot.model.photo
import ru.zveron.contract.parameter.internal.infoCategory
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

    fun mockCreateLot(
        communicationChannel: CommunicationChannel = CommunicationChannel.CHAT,
        price: Int = 5,
        description: String = "description",
        title: String = "title",
        gender: ru.zveron.contract.lot.model.Gender = ru.zveron.contract.lot.model.Gender.MALE
    ) = createLotRequest {
        this.title = title
        this.description = description
        photos.addAll(listOf(
            photo {
                url = "someurl"
                order = 0
            }, photo {
                url = "someurl"
                order = 1
            })
        )
        parameters.putAll(mapOf(1 to "1", 2 to "2"))
        this.price = price
        this.communicationChannel.add(communicationChannel)
        this.gender = gender
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

    fun mockEditLotRequest(
        communicationChannel: CommunicationChannel,
        price: Int,
        description: String = "description",
        title: String = "title"
    ) = editLotRequest {
        this.title = title
        this.description = description
        photos.addAll(listOf(photo {
            photo {
                url= "someurl"
                order = 0
            }
        }))
        this.price = price
        this.communicationChannel.add(communicationChannel)
    }

    fun mockInfoCategory(
        hasGender: Boolean = true,
        hasChildren: Boolean = false
    ) = infoCategory {
        this.hasGender = hasGender
        this.hasChildren = hasChildren
    }
}
