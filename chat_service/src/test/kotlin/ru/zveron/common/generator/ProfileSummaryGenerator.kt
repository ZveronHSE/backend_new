package ru.zveron.common.generator

import ru.zveron.common.generator.PrimitivesGenerator.generateLong
import ru.zveron.common.generator.PrimitivesGenerator.generateString
import ru.zveron.contract.profile.profileSummary

object ProfileSummaryGenerator {

    fun generateProfile(id: Long) = profileSummary {
        this.id = id
        name = generateString(10)
        surname = generateString(10)
        imageUrl = generateString(20)
        addressId = generateLong()
    }
}