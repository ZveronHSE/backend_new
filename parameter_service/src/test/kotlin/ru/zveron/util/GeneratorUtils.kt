package ru.zveron.util

import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import ru.zveron.entity.ParameterFromType
import ru.zveron.model.ParameterType
import ru.zveron.util.CreateEntitiesUtils.mockParameterFromType

object GeneratorUtils {
    fun generateString(size: Int = 10): String = RandomStringUtils.randomAlphanumeric(size)

    fun generateBoolean() = RandomUtils.nextBoolean()

    fun generateInt() = RandomUtils.nextInt()

    fun generateParameterFromType(n: Int = 5): List<ParameterFromType> = List(n) {
        mockParameterFromType(
            id = it,
            name = generateString(),
            isRequired = generateBoolean(),
            listValue = List(it) { generateString() },
            type = ParameterType.values().random().name
        )
    }

}