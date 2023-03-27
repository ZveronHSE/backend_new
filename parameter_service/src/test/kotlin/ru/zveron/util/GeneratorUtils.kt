package ru.zveron.util

import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import ru.zveron.contract.parameter.model.Parameter
import ru.zveron.contract.parameter.model.Type
import ru.zveron.entity.Category
import ru.zveron.entity.ParameterFromType
import ru.zveron.model.ParameterType
import ru.zveron.util.CreateEntitiesUtils.mockParameterFromType
import java.time.Instant
import java.util.Date

object GeneratorUtils {
    fun generateString(size: Int = 10): String = RandomStringUtils.randomAlphanumeric(size)

    fun generateBoolean() = RandomUtils.nextBoolean()

    fun generateInt() = RandomUtils.nextInt()

    fun generateCategories(n: Int = 5): List<Category> = List(n) {
        Category(id = RandomUtils.nextInt(), name = generateString(), imageUrl = "")
    }

    fun generateParameterFromType(n: Int = 5): List<ParameterFromType> = List(n) {
        mockParameterFromType(
            id = it,
            name = generateString(),
            isRequired = generateBoolean(),
            listValue = List(it) { generateString() },
            type = ParameterType.values().random().name
        )
    }

    fun List<Parameter>.buildMapParameterValues(): MutableMap<Int, String> {
        val parametersMap = mutableMapOf<Int, String>()

        for (parameter in this) {
            if (parameter.valuesCount == 0) {
                val value = when (parameter.type) {
                    Type.STRING -> generateString()
                    Type.INTEGER -> generateInt()
                    Type.DATE -> Date.from(Instant.now()).toInstant()
                    else -> {}
                }

                parametersMap[parameter.id] = value.toString()
            } else {
                parametersMap[parameter.id] = parameter.valuesList.random()
            }
        }

        return parametersMap
    }

}