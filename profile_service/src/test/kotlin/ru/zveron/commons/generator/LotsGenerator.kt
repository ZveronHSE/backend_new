package ru.zveron.commons.generator

import ru.zveron.commons.generator.PropsGenerator.generateLongId
import ru.zveron.commons.generator.PropsGenerator.generateString
import ru.zveron.contract.core.lot

object LotsGenerator {

    fun generateLot(favorite: Boolean) = lot {
        id = generateLongId()
        title = generateString(10)
        price = generateString(5)
        publicationDate = generateString(5)
        photoId = generateLongId()
        this.favorite = favorite
    }
}