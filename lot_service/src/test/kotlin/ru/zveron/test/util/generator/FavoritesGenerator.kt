package ru.zveron.test.util.generator

import ru.zveron.favorites.lot.lotsExistInFavoritesResponse
import ru.zveron.test.util.GeneratorUtils.generateBooleans

object FavoritesGenerator {

    fun generateLotExistInFavoritesResponse(sizeExists: Int) = lotsExistInFavoritesResponse {
        isExists.addAll(generateBooleans(sizeExists))
    }
}