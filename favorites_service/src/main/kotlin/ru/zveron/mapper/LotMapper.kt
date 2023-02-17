package ru.zveron.mapper

import ru.zveron.contract.lot.model.Lot
import ru.zveron.contract.lot.model.Status
import ru.zveron.favorites.lot.LotStatus
import ru.zveron.favorites.lot.LotSummary
import ru.zveron.favorites.lot.lotSummary

object LotMapper {

    fun Lot.toSummary(): LotSummary = lotSummary {
        id = this@toSummary.id
        title = this@toSummary.title
        priceFormatted = price
        publicationDateFormatted = publicationDate
        status = this@toSummary.status.toFavoritesStatus()
        firstImage = photoId
    }

    fun Status.toFavoritesStatus(): LotStatus = when (this) {
        Status.ACTIVE -> LotStatus.ACTIVE
        Status.CLOSED, Status.CANCELED -> LotStatus.CLOSED
        else -> LotStatus.UNRECOGNIZED
    }
}