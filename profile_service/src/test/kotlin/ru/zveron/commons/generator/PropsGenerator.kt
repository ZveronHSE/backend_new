package ru.zveron.commons.generator

import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import java.util.UUID

object PropsGenerator {

    fun generateLongId() = RandomUtils.nextLong()

    fun generateNIds(n: Int) = List(n) { generateLongId() }

    fun generateString(n: Int) = RandomStringUtils.random(n)

    fun generateDouble() = RandomUtils.nextDouble(0.0, 180.0) * if (RandomUtils.nextBoolean()) 1 else -1

    fun generateImageUrl() = """http://yandex.cloud/${UUID.randomUUID()}.png"""
}
