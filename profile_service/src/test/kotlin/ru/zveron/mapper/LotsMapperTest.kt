package ru.zveron.mapper

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import ru.zveron.LotStatus
import ru.zveron.commons.assertions.lotShouldBe
import ru.zveron.commons.generator.LotsGenerator

class LotsMapperTest {

    @Test
    fun `lot2Builder maps correctly active lots`() {
        val expectedActive = LotsGenerator.generateLot(false)

        val actualActive = LotsMapper.lot2Builder(listOf(expectedActive), LotStatus.ACTIVE)

        actualActive.apply {
            size shouldBe 1
            first() lotShouldBe expectedActive
        }
    }

    @Test
    fun `lot2Builder maps correctly closed lots`() {
        val expectedClosed = LotsGenerator.generateLot(false)

        val actualClosed = LotsMapper.lot2Builder(listOf(expectedClosed), LotStatus.CLOSED)

        actualClosed.apply {
            size shouldBe 1
            first() lotShouldBe expectedClosed
        }
    }
}