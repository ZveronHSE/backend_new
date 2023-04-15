package ru.zveron.test.util

import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import java.util.UUID

object GeneratorUtils {

    fun generateInts(n: Int = 5) = List(n) { generateInt() }
    fun generateIds(n: Int = 5) = List(n) { generateLong() }

    fun generateBooleans(n: Int = 5) = List(n) { generateBoolean() }

    fun generateBoolean() = RandomUtils.nextBoolean()

    fun generateInt() = RandomUtils.nextInt(1, 20)

    fun generateLong(start: Long = 0) = RandomUtils.nextLong(start, Long.MAX_VALUE)

    fun generateString(size: Int = 10): String = RandomStringUtils.randomAlphanumeric(size)

    fun generateDouble() = RandomUtils.nextDouble(0.0, 180.0) * if (RandomUtils.nextBoolean()) 1 else -1

    fun generateImageUrl() = """https://yandex.cloud/${UUID.randomUUID()}.jpeg"""
}
