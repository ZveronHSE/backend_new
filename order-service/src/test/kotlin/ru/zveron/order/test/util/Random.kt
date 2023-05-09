package ru.zveron.order.test.util

import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import java.util.UUID


fun randomId() = RandomUtils.nextLong()

fun randomSubwayId() = RandomUtils.nextInt()

fun randomName() = "name-${UUID.randomUUID()}"

fun randomSurname() = "surname-${UUID.randomUUID()}"

fun randomImageUrl() = """https://${RandomStringUtils.randomAlphanumeric(10)}.com"""

inline fun <reified T : Enum<T>> randomEnum() = enumValues<T>().random()
