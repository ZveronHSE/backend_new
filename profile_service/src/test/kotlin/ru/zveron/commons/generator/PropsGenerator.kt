package ru.zveron.commons.generator

import org.apache.commons.lang3.RandomUtils

object PropsGenerator {

    fun generateLongId() = RandomUtils.nextLong()

    fun generateNIds(n: Int) = List(n) { generateLongId() }

    fun generateString(n: Int) = String(CharArray(n) {
        RandomUtils.nextInt('a'.code, 'z'.code + 1).toChar()
            .let { if (RandomUtils.nextBoolean()) it.uppercaseChar() else it }
    })

    fun generateDouble() = RandomUtils.nextDouble(0.0, 180.0) * if (RandomUtils.nextBoolean()) 1 else -1
}