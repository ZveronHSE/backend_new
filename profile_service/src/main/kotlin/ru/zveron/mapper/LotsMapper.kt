package ru.zveron.mapper

import ru.zveron.contract.profile.LotStatus
import ru.zveron.contract.profile.LotSummary
import ru.zveron.contract.lot.model.Lot
import ru.zveron.contract.profile.lotSummary

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
                isFavorite = lot.favorite
            }
        }
}