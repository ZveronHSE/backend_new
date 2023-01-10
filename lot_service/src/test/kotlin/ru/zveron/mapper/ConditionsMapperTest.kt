package ru.zveron.mapper

import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.zveron.contract.lot.TypeSort
import ru.zveron.model.search.table.LOT
import ru.zveron.test.util.GeneratorUtils
import ru.zveron.test.util.model.WaterfallEntities
import java.util.stream.Stream


class ConditionsMapperTest {
    companion object {
        @JvmStatic
        private fun parametersForSeek(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(true, TypeSort.ASC),
                Arguments.of(true, TypeSort.DESC),
                Arguments.of(false, TypeSort.ASC),
                Arguments.of(false, TypeSort.DESC),
            )
        }
    }

    @ParameterizedTest
    @MethodSource("parametersForSeek")
    fun `Building conditions search with only parameters for seek method`(isSortByDate: Boolean, typeSort: TypeSort) {
        val pageSize = GeneratorUtils.generateInt()

        val waterfallRequest = WaterfallEntities.mockWaterfallRequest(
            pageSize = pageSize,
            isSortByDate = isSortByDate,
            typeSort = typeSort,
            lotId = GeneratorUtils.generateLong(),
            lotValue = GeneratorUtils.generateLong()
        )
        val (sorts, values) = WaterfallEntities.buildParametersForSeekMethod(waterfallRequest)
        val conditionsSearch = ConditionsMapper.parse(waterfallRequest, categories = null, sellerId = null)

        conditionsSearch.pageSize shouldBe pageSize
        conditionsSearch.seekMethod.values shouldBe values
        conditionsSearch.seekMethod.sorts shouldBe sorts
    }


    @Test
    fun `Build only one condition for search text in title of lots`() {
        val query = GeneratorUtils.generateString(10)
        val waterfallRequest = WaterfallEntities.mockWaterfallRequest(query = query)

        val conditionsSearch = ConditionsMapper.parse(waterfallRequest, categories = null, sellerId = null)
        conditionsSearch.conditions.asClue {
            it.size shouldBe 1
            it shouldContainExactly listOf(LOT.TITLE.likeIgnoreCase("%$query%"))
        }
    }


    @Test
    fun `If pass categories to parser, conditions search should has categories`() {
        val categories = GeneratorUtils.generateInts(5)
        val waterfallRequest = WaterfallEntities.mockWaterfallRequest()

        val conditionsSearch = ConditionsMapper.parse(waterfallRequest, categories, sellerId = null)

        conditionsSearch.categories shouldContainExactly categories
    }


    @Test
    fun `If pass sellerId to parser, build only one condition for delete lots by seller id`() {
        val sellerId = GeneratorUtils.generateLong(1)
        val waterfallRequest = WaterfallEntities.mockWaterfallRequest()

        val conditionsSearch = ConditionsMapper.parse(waterfallRequest, categories = null, sellerId = sellerId)

        conditionsSearch.conditions shouldContainExactly listOf(LOT.SELLER_ID.notEqual(sellerId))
    }
}