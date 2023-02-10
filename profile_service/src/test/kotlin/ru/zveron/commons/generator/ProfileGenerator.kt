package ru.zveron.commons.generator

import ru.zveron.commons.generator.PropsGenerator.generateString
import ru.zveron.commons.generator.PropsGenerator.generateLongId
import ru.zveron.entity.Profile
import java.time.Instant

object ProfileGenerator {

    fun generateProfile(lastSeen: Instant, addressId: Long = -1) = Profile(
        name = generateString(20),
        surname = generateString(20),
        imageId = generateLongId(),
        lastSeen = lastSeen,
        addressId = addressId,
    )
}