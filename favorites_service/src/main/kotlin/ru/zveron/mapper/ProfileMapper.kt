package ru.zveron.mapper

import ru.zveron.contract.profile.ProfileSummary
import ru.zveron.favorites.profile.profileSummary

object ProfileMapper {

    fun ProfileSummary.toFavoritesSummary(rating: Double) = profileSummary {
        id = this@toFavoritesSummary.id
        name = this@toFavoritesSummary.name
        surname = this@toFavoritesSummary.surname
        imageId = this@toFavoritesSummary.imageId
        this.rating = rating
    }
}