package ru.zveron.mapper

import io.grpc.Status
import ru.zveron.contract.lot.CardLot
import ru.zveron.contract.lot.cardLot
import ru.zveron.contract.lot.channelLink
import ru.zveron.contract.lot.contact
import ru.zveron.contract.lot.model.parameter
import ru.zveron.contract.lot.model.photo
import ru.zveron.contract.lot.seller
import ru.zveron.entity.Lot
import ru.zveron.exception.LotException
import ru.zveron.mapper.LotMapper.toContract
import ru.zveron.mapper.LotMapper.toFormattingPrice
import ru.zveron.mapper.SellerMapper.toCommunicationChannels
import ru.zveron.model.Address
import ru.zveron.model.SellerProfile

class CardLotBuilder(
    var lot: Lot? = null,
    var isFavoriteLot: Boolean = false,
    var isOwnLot: Boolean = false,
    var seller: SellerProfile? = null,
    var address: Address? = null
) {
    companion object {
        inline fun cardLotBuilder(init: CardLotBuilder.() -> Unit) =
            CardLotBuilder()
                .apply(init)
                .build()
    }

    fun build(): CardLot {
        if (lot == null) {
            throw LotException(Status.INVALID_ARGUMENT, "Don't get lot for building CardLot")
        }
        val lot = lot!!

        if (seller == null) {
            throw LotException(Status.INVALID_ARGUMENT, "Don't get seller for building CardLot")
        }
        val seller = seller!!

        if (address == null) {
            throw LotException(Status.INVALID_ARGUMENT, "Don't get address for building CardLot")
        }
        val address = address!!

        return cardLot {
            id = lot.id
            title = lot.title
            photos.addAll(lot.photos.map {
                photo {
                    id = it.id
                    order = it.order
                }
            })
            lot.gender?.let { gender = it.toContract() }
//            address
            parameters.addAll(lot.parameters.map {
                parameter {
                    id = it.id.parameter
                    value = it.value
                }
            })
            description = lot.description
            price = lot.price.toFormattingPrice()
            favorite = isFavoriteLot
            own = isOwnLot
            // TODO canAddReview ZV-300
            contact = contact {
                communicationChannel.addAll(seller.contact.toCommunicationChannels())
                channelLink = channelLink {
                    seller.channelLink.vk?.let { vk = it }
                    seller.channelLink.email?.let { email = it }
                    seller.channelLink.phone?.let { phone = it }
                }
            }
            this.seller = seller {
                id = seller.id
                name = seller.name
                surname = seller.surname
//           TODO     rating = seller ZV-304
                online = seller.isOnline
                photoId = seller.imageId
            }
        }
    }
}