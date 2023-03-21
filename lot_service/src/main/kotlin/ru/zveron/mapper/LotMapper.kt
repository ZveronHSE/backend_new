package ru.zveron.mapper

import com.google.protobuf.util.Timestamps
import ru.zveron.contract.address.AddressResponse
import ru.zveron.contract.core.Status
import ru.zveron.contract.core.lot
import ru.zveron.contract.lot.LotsIdResponse
import ru.zveron.contract.lot.ProfileLotsResponse
import ru.zveron.contract.lot.WaterfallResponse
import ru.zveron.contract.lot.lotsIdResponse
import ru.zveron.contract.lot.profileLotsResponse
import ru.zveron.contract.lot.waterfallResponse
import ru.zveron.entity.Lot
import ru.zveron.exception.LotException
import ru.zveron.model.Address
import ru.zveron.model.SummaryLot
import ru.zveron.model.enum.Gender
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

object LotMapper {
    /**
     * Форматирует в виде 15 января 2022
     */
    private val date = SimpleDateFormat("d MMMM y", Locale("ru"))
    private const val CATEGORY_ID_ANIMAL = 1
    private const val CATEGORY_ID_GOOD = 2

    fun buildLotsIdResponse(entityLots: List<Lot>, favorites: List<Boolean>?): LotsIdResponse {
        val lots = buildLotsContract(entityLots, favorites)

        return lotsIdResponse {
            this.lots.addAll(lots)
        }
    }

    fun buildWaterfallResponse(summaryLots: List<SummaryLot>, favorites: List<Boolean>?): WaterfallResponse {
        val lots = summaryLots.mapIndexed { index, summaryLot ->
            lot {
                id = summaryLot.id
                title = summaryLot.title
                price = summaryLot.price.toFormattingPrice()
                publicationDate = summaryLot.createdAt.toFormattingDate()
                favorites?.let { favorite = it[index] }
            }
        }


        val lastLot = summaryLots.last()

        return waterfallResponse {
            this.lots.addAll(lots)

            this.lastLot = ru.zveron.contract.lot.lastLot {
                id = lastLot.id
                price = lastLot.price
                date = Timestamps.fromMillis(lastLot.createdAt.toEpochMilli())
            }
        }
    }

    fun Int.toFormattingPrice(): String {
        return if (this == 0) "Договорная" else "$this ₽"
    }

    fun Gender.toContract(): ru.zveron.contract.lot.model.Gender {
        return when (this) {
            Gender.FEMALE -> ru.zveron.contract.lot.model.Gender.FEMALE
            Gender.MALE -> ru.zveron.contract.lot.model.Gender.MALE
            Gender.METIS -> ru.zveron.contract.lot.model.Gender.METIS
        }
    }


    fun buildProfileLotsResponse(entityLots: List<Lot>, favorites: List<Boolean>?): ProfileLotsResponse {
        val lots = buildLotsContract(entityLots, favorites)

        val groupingLots = lots.groupBy { it.status }

        return profileLotsResponse {
            groupingLots[Status.ACTIVE]?.let { activateLots.addAll(it) }
            groupingLots[Status.CLOSED]?.let { inactivateLots.addAll(it) }
        }
    }

    fun AddressResponse.toAddress(): Address {
        // TODO Потом придумаю как это красиво сделать
        val address = arrayOf(region, district, town, street, house)
            .filter { !it.isNullOrBlank() }
            .joinToString()

        return Address(id, address, latitude, longitude)
    }

    fun ru.zveron.contract.lot.model.Gender.toGender(): Gender {
        return when (this) {
            ru.zveron.contract.lot.model.Gender.FEMALE -> Gender.FEMALE
            ru.zveron.contract.lot.model.Gender.MALE -> Gender.MALE
            ru.zveron.contract.lot.model.Gender.METIS -> Gender.METIS
            else -> throw LotException(io.grpc.Status.INVALID_ARGUMENT, "Gender didn't detect")
        }
    }

    private fun Instant.toFormattingDate() = date.format(Date.from(this))

    private fun buildLotsContract(entityLots: List<Lot>, favorites: List<Boolean>?) = entityLots
        .mapIndexed { index, lot ->
            lot {
                id = lot.id
                title = lot.title
                price = lot.price.toFormattingPrice()
                publicationDate = lot.createdAt.toFormattingDate()
                favorites?.let { favorite = it[index] }
                status = Status.forNumber(lot.status.ordinal)
                categoryId = if (lot.gender == null) CATEGORY_ID_ANIMAL else CATEGORY_ID_GOOD
            }
        }
}

