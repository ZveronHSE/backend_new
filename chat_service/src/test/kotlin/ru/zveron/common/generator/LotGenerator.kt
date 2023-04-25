package ru.zveron.common.generator

import ru.zveron.common.generator.PrimitivesGenerator.generateString
import ru.zveron.contract.core.Status
import ru.zveron.contract.core.lot

object LotGenerator {

    fun generateLot(id: Long) = lot {
        this.id = id
        title = generateString(10)
        price = generateString(5)
        publicationDate = generateString(5)
        imageUrl = generateString(20)
        favorite = false
        status = Status.ACTIVE
        categoryId = 1
    }
}