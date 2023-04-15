package ru.zveron.commons.generator

import org.apache.commons.lang3.RandomUtils
import java.util.UUID

object PropsGenerator {

    fun generateLongId() = RandomUtils.nextLong()

    fun generateNIds(n: Int) = List(n) { generateLongId() }

    fun generateString(n: Int) = String(CharArray(n) {
        RandomUtils.nextInt('a'.code, 'z'.code + 1).toChar()
            .let { if (RandomUtils.nextBoolean()) it.uppercaseChar() else it }
    })

    fun generateDouble() = RandomUtils.nextDouble(0.0, 180.0) * if (RandomUtils.nextBoolean()) 1 else -1

    fun generateImageUrl() = """http://yandex.cloud/${UUID.randomUUID()}.png"""

    fun ratingGenerator() = RandomUtils.nextDouble(0.0, 5.0)
}
