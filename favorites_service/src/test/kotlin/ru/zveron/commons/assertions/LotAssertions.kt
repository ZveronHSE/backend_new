package ru.zveron.commons.assertions

import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import ru.zveron.contract.lot.model.Lot
import ru.zveron.favorites.lot.LotSummary
import ru.zveron.mapper.LotMapper.toFavoritesStatus

object LotAssertions {

    infix fun List<LotSummary>.lotsShouldBe(expected: List<Lot>) {
        this.size shouldBe expected.size
        this.forEach { actualLot ->
            val expectedLot = expected.firstOrNull { it.id == actualLot.id }
                ?: fail("Lot with id: ${actualLot.id} is missed in expected lots")
            actualLot lotShouldBe expectedLot
        }
    }

    private infix fun LotSummary.lotShouldBe(expected: Lot) {
        id shouldBe expected.id
        title shouldBe expected.title
        priceFormatted shouldBe expected.price
        publicationDateFormatted shouldBe expected.publicationDate
        status shouldBe expected.status.toFavoritesStatus()
        firstImage shouldBe expected.photoId
    }
}