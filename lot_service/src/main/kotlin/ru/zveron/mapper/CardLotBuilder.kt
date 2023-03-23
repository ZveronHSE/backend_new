package ru.zveron.mapper

import io.grpc.Status
import ru.zveron.contract.lot.CardLot
import ru.zveron.contract.lot.Contact
import ru.zveron.contract.lot.Seller
import ru.zveron.contract.lot.cardLot
import ru.zveron.contract.lot.channelLink
import ru.zveron.contract.lot.contact
import ru.zveron.contract.lot.model.Parameter
import ru.zveron.contract.lot.model.parameter
import ru.zveron.contract.lot.model.photo
import ru.zveron.contract.lot.seller
import ru.zveron.contract.lot.statistics
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
    var address: Address? = null,
    var parametersMap: Map<Int, String> = mapOf()
) {
    companion object {
        inline fun buildCardLot(init: CardLotBuilder.() -> Unit) =
            CardLotBuilder()
                .apply(init)
                .build()
    }

    fun build(): CardLot {
        if (lot == null) {
            throw LotException(Status.INVALID_ARGUMENT, "Don't get lot for building CardLot")
        }
        val lot = lot!!

        if (address == null) {
            throw LotException(Status.INVALID_ARGUMENT, "Don't get address for building CardLot")
        }

        return cardLot {
            id = lot.id
            title = lot.title
            photos.addAll(lot.photos.map {
                photo {
                    id = it.id
                    order = it.orderPhoto
                }
            })
            lot.gender?.let { gender = it.toContract() }
            this.address = buildAddress()
            parameters.addAll(buildParameters())
            description = lot.description
            price = lot.price.toFormattingPrice()
            favorite = isFavoriteLot
            own = isOwnLot
            // TODO canAddReview ZV-300
            contact = buildContact()
            this.seller = buildSeller()

            statistics = statistics {
                view = lot.statistics.quantityView
                // TODO add favorite
            }
        }
    }

    private fun buildParameters(): List<Parameter> {
        return lot!!.parameters.mapNotNull {
            val lotParameter = it

            parametersMap[it.id.parameter]?.let {
                parameter {
                    id = lotParameter.id.parameter
                    name = it
                    value = lotParameter.value
                }
            }
        }
    }

    private fun buildAddress(): ru.zveron.contract.lot.model.Address {
        val address = address!!

        return ru.zveron.contract.lot.model.address {
            this.address = address.address
            latitude = address.latitude
            longitude = address.longitude
        }
    }

    private fun buildContact(): Contact {
        if (seller == null) {
            return contact { }
        }

        val seller = seller!!

        return contact {
            communicationChannel.addAll(seller.contact.toCommunicationChannels())
            channelLink = channelLink {
                seller.channelLink.vk?.let { vk = it }
                seller.channelLink.email?.let { email = it }
                seller.channelLink.phone?.let { phone = it }
            }
        }
    }

    private fun buildSeller(): Seller {
        if (seller == null) {
            return seller {}
        }

        val seller = seller!!

        return seller {
            id = seller.id
            name = seller.name
            surname = seller.surname
//           TODO     rating = seller ZV-304
            online = seller.isOnline
            photoId = seller.imageId
        }
    }

}