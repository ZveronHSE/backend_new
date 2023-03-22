package ru.zveron.common.generator

import com.datastax.oss.driver.api.core.uuid.Uuids
import org.apache.commons.lang3.RandomUtils
import java.util.UUID

object PrimitivesGenerator {

    fun generateLong() = RandomUtils.nextLong()

    fun generateLongs(n: Int) = List(n) { generateLong() }

    fun generateString(n: Int) = String(CharArray(n) {
        RandomUtils.nextInt('a'.code, 'z'.code + 1).toChar()
            .let { if (RandomUtils.nextBoolean()) it.uppercaseChar() else it }
    })

    fun generateDouble() = RandomUtils.nextDouble(0.0, 180.0) * if (RandomUtils.nextBoolean()) 1 else -1

    fun generateNUuids(n: Int) = List(n) { UUID.randomUUID() }

    fun generateNTimeUuids(n: Int) = List(n) { Uuids.timeBased() }
}