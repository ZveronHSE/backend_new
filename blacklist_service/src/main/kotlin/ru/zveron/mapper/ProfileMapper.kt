package ru.zveron.mapper

import ru.zveron.contract.blacklist.profileSummary
import ru.zveron.contract.profile.ProfileSummary

object ProfileMapper {

    fun ProfileSummary.toResponse() = profileSummary {
        id = this@toResponse.id
        name = this@toResponse.name
        surname = this@toResponse.surname
        imageUrl = this@toResponse.imageUrl
        addressId = this@toResponse.addressId
    }
}