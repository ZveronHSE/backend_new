package ru.zveron.commons.generator

import ru.zveron.commons.generator.PropsGenerator.generateString
import ru.zveron.commons.generator.PropsGenerator.generateUserId
import ru.zveron.entity.Profile
import java.time.Instant

object ProfileGenerator {

    fun generateProfile(profileId: Long, lastSeen: Instant, addressId: Long = -1) = Profile(
        id = profileId,
        name = generateString(20),
        surname = generateString(20),
        imageId = generateUserId(),
        lastSeen = lastSeen,
        addressId = addressId,
    )
}