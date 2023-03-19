package ru.zveron.commons.generator

import ru.zveron.commons.generator.PropsGenerator.generateString
import ru.zveron.commons.generator.PropsGenerator.generateLongId
import ru.zveron.entity.Profile
import java.time.Instant

object ProfileGenerator {

    fun generateProfile(lastSeen: Instant, addressId: Long = 0, addPassword: Boolean = false) = Profile(
        name = generateString(20),
        surname = generateString(20),
        imageId = generateLongId(),
        lastSeen = lastSeen,
        addressId = addressId,
        passwordHash = if (addPassword) generateString(32) else null
    )
}