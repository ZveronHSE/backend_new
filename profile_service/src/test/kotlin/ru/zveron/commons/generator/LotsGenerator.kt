package ru.zveron.commons.generator

import ru.zveron.commons.generator.PropsGenerator.generateString
import ru.zveron.commons.generator.PropsGenerator.generateUserId
import ru.zveron.contract.lot.lot

object LotsGenerator {

    fun generateLot(favorite: Boolean) = lot {
        id = generateUserId()
        title = generateString(10)
        price = generateString(5)
        publicationDate = generateString(5)
        photoId = generateUserId()
        isFavorite = favorite
    }
}