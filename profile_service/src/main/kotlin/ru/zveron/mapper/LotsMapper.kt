package ru.zveron.mapper

import ru.zveron.LotStatus
import ru.zveron.LotSummary
import ru.zveron.contract.lot.Lot
import ru.zveron.lotSummary

object LotsMapper {

    fun lot2Builder(lots: List<Lot>, status: LotStatus): Collection<LotSummary> =
        lots.map { lot ->
            lotSummary{
                id = lot.id
                title = lot.title
                priceFormatted = lot.price
                publicationDateFormatted = lot.publicationDate
                this.status = status
                firstImage = lot.photoId
                isFavorite = lot.isFavorite
            }
        }
}