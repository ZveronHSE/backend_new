package ru.zveron.order.test.util

import org.apache.commons.lang3.RandomUtils
import java.util.UUID


fun randomId() = RandomUtils.nextLong()

fun randomName() = "name-${UUID.randomUUID()}"

fun randomSurname() = "surname-${UUID.randomUUID()}"

inline fun <reified T : Enum<T>> randomEnum() = enumValues<T>().random()
