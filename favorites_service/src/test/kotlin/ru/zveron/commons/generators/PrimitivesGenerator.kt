package ru.zveron.commons.generators

import org.apache.commons.lang3.RandomUtils

object PrimitivesGenerator {

    fun generateUserId() = RandomUtils.nextLong()

    fun generateNIds(n: Int) = List(n) { generateUserId() }

    fun generateString(length: Int) = String(CharArray(length) {
        RandomUtils.nextInt('a'.code, 'z'.code + 1).toChar()
            .let { if (RandomUtils.nextBoolean()) it.uppercaseChar() else it }
    })
}