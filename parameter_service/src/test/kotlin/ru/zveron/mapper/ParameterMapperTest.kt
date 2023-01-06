package ru.zveron.mapper

import io.kotest.assertions.forEachAsClue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import ru.zveron.mapper.ParameterMapper.toResponse
import ru.zveron.util.GeneratorUtils.generateParameterFromType

internal class ParameterMapperTest {
    @ParameterizedTest
    @ValueSource(ints = [0, 3])
    fun `Validate mapping from ParameterFromType to ParameterResponse`(quantity: Int) {
        val mock = generateParameterFromType(quantity)

        val resultActual = mock.toResponse()

        // Подумать, как тут потестить
        resultActual.parametersList.forEachAsClue {
//            it.id =
        }
    }
}