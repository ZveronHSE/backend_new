package ru.zveron.mapper

import io.kotest.assertions.asClue
import io.kotest.assertions.forEachAsClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import ru.zveron.contract.parameter.Type
import ru.zveron.mapper.ParameterMapper.toResponse
import ru.zveron.util.CreateEntitiesUtils
import ru.zveron.util.GeneratorUtils
import ru.zveron.util.GeneratorUtils.generateParameterFromType

internal class ParameterMapperTest {
    @ParameterizedTest
    @ValueSource(ints = [0, 10])
    fun `Validate mapping from ParameterFromType to ParameterResponse`(quantity: Int) {
        val parametersMock = generateParameterFromType(quantity)

        val parametersActual = parametersMock.toResponse()

        parametersActual.parametersCount shouldBe quantity

        parametersActual.parametersList.forEachAsClue { parameterActual ->
            val parameterExpected = parametersMock
                .find { mockParameter -> mockParameter.id.parameter == parameterActual.id }
                ?.parameter

            parameterExpected.shouldNotBeNull()

            parameterActual.asClue {
                it.name shouldBe parameterExpected.name
                it.type shouldBe Type.valueOf(parameterExpected.type)
                it.isRequired shouldBe parameterExpected.isRequired
                it.valuesList shouldBe parameterExpected.list_value
            }
        }
    }

    @Test
    fun `Should throw exception, if unknown type for values`() {
        val parameterFromTypeMock = listOf(
            CreateEntitiesUtils.mockParameterFromType(
                id = 1,
                name = GeneratorUtils.generateString(),
                isRequired = GeneratorUtils.generateBoolean(),
                listValue = listOf(),
                type = "UNKNOWN"
            )
        )

        shouldThrow<IllegalArgumentException> { parameterFromTypeMock.toResponse() }
    }
}