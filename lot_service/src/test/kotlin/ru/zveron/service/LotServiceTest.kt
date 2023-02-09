package ru.zveron.service

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.DataBaseTest
import ru.zveron.exception.LotException
import ru.zveron.model.constant.LotStatus
import ru.zveron.repository.LotRepository
import ru.zveron.test.util.GeneratorUtils
import ru.zveron.test.util.GeneratorUtils.generateIds
import ru.zveron.test.util.generator.ProfileGenerator.generateSellerProfile
import ru.zveron.test.util.model.LotEntities
import java.time.Instant

class LotServiceTest : DataBaseTest() {
    @Autowired
    lateinit var lotService: LotService

    @Autowired
    lateinit var lotRepository: LotRepository

    @Test
    fun `Get lot by id, if exists`() {
        val expectedLot = lotRepository.save(LotEntities.mockLotEntity())
        val actualLot = lotService.getLotById(expectedLot.id)

        actualLot shouldBe expectedLot
    }

    @Test
    fun `Throw exception, if didnt find lot by id`() {
        shouldThrow<LotException> { lotService.getLotById(GeneratorUtils.generateLong(1)) }
    }

    @ParameterizedTest
    @ValueSource(ints = [-10, -5, 0])
    fun `Throw exception, if lot id was negative number`(lotId: Long) {
        shouldThrow<LotException> { lotService.getLotById(lotId) }
    }

    @Test
    fun `Get lots by ids`() {
        val lot1 = lotRepository.save(LotEntities.mockLotEntity())
        val lot2 = lotRepository.save(LotEntities.mockLotEntity())
        val lots = lotService.getLotsByIds(listOf(lot1.id, lot2.id, GeneratorUtils.generateLong(1)))

        lots shouldContainExactlyInAnyOrder listOf(lot1, lot2)
    }


    @Test
    fun `Throw exception, if any lot ids was negative number`() {
        shouldThrow<LotException> { lotService.getLotsByIds(listOf(5, 7, 1, -2, 6)) }
    }

    @Test
    fun `GetLotsBySellerId correct get lots by seller id and order by date`() {
        val sellerId = GeneratorUtils.generateLong(1)

        val lot1 = lotRepository.save(
            LotEntities.mockLotEntity(
                sellerId = sellerId,
                dateCreation = Instant.now().minusSeconds(500L)
            )
        )
        lotRepository.save(LotEntities.mockLotEntity(sellerId = GeneratorUtils.generateLong(1)))
        val lot2 = lotRepository.save(LotEntities.mockLotEntity(sellerId = sellerId))

        val lots = lotService.getLotsBySellerId(sellerId)

        lots shouldContainExactly listOf(lot2, lot1)
    }

    @ParameterizedTest
    @ValueSource(ints = [-10, -5, 0])
    fun `GetLotsBySellerId Throw exception, if seller id was negative number`(sellerId: Long) {
        shouldThrow<LotException> { lotService.getLotsBySellerId(sellerId) }
    }


    @Test
    fun `CreateLot correct creating lot`() {
        val request = LotEntities.mockCreateLot()
        val (sellerId, addressId) = generateIds(2)
        val lot = lotService.createLot(request, generateSellerProfile(sellerId), addressId)

        lot.asClue {
            it.dateCreation.shouldBeAfter(Instant.now().minusSeconds(3))
            it.lotFormId shouldBe request.lotFormId
            it.categoryId shouldBe request.categoryId
            it.title shouldBe request.title
            it.description shouldBe request.description
            it.addressId shouldBe addressId
            it.sellerId shouldBe sellerId
            it.price shouldBe request.price
            it.status shouldBe LotStatus.ACTIVE
            it.gender?.name shouldBe request.gender.name
            // TODO parameters
            // TODO statistics
            // TODO photo
        }
    }
}