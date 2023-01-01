package ru.zveron.commons.generators

import org.apache.commons.lang3.RandomUtils

object IdsGenerator {

    fun generateUserId() = RandomUtils.nextLong()

    fun generateNIds(n: Int) = List(n) { generateUserId() }
}