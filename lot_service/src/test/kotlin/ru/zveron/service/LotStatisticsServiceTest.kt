package ru.zveron.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.DataBaseTest
import ru.zveron.exception.LotException
import ru.zveron.repository.LotRepository
import ru.zveron.repository.LotStatisticsRepository
import ru.zveron.test.util.model.LotEntities

class LotStatisticsServiceTest : DataBaseTest() {
    @Autowired
    lateinit var lotStatisticsService: LotStatisticsService

    @Autowired
    lateinit var lotRepository: LotRepository

    @Autowired
    lateinit var lotStatisticsRepository: LotStatisticsRepository

    @Test
    fun `IncrementViewCounter increasing view counter for lot`() {
        val lot = lotRepository.save(LotEntities.mockLotEntity())
        val lotStatistics = lotStatisticsRepository.save(LotEntities.mockLotStatistics(lot))

        lotStatisticsService.incrementViewCounter(lot.id)

        val newLotStatistics = lotStatisticsRepository.getReferenceById(lot.id)
        newLotStatistics.quantityView shouldBe lotStatistics.quantityView + 1
    }

    @ParameterizedTest
    @ValueSource(ints = [-10, -5, 0])
    fun `IncrementViewCounter should throw exception if get negative id`(lotId: Long) {
        shouldThrow<LotException> { lotStatisticsService.incrementViewCounter(lotId) }
    }
}