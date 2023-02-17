package ru.zveron.mapper

import ru.zveron.contract.profile.ProfileSummary
import ru.zveron.favorites.profile.profileSummary

object ProfileMapper {

    fun ProfileSummary.toFavoritesSummary() = profileSummary {
        id = this@toFavoritesSummary.id
        name = this@toFavoritesSummary.name
        surname = this@toFavoritesSummary.surname
        imageId = this@toFavoritesSummary.imageId
        addressId = this@toFavoritesSummary.addressId
    }
}