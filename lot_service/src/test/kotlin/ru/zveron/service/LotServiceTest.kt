package ru.zveron.service

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.DataBaseTest
import ru.zveron.contract.lot.ClosingLotReason
import ru.zveron.contract.lot.closeLotRequest
import ru.zveron.contract.lot.model.CommunicationChannel
import ru.zveron.exception.LotException
import ru.zveron.model.enum.LotStatus
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
                createdAt = Instant.now().minusSeconds(500L)
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
            it.createdAt.shouldBeAfter(Instant.now().minusSeconds(3))
            it.lotFormId shouldBe request.lotFormId
            it.categoryId shouldBe request.categoryId
            it.title shouldBe request.title
            it.description shouldBe request.description
            it.addressId shouldBe addressId
            it.sellerId shouldBe sellerId
            it.price shouldBe request.price
            it.status shouldBe LotStatus.ACTIVE
            it.gender?.name shouldBe request.gender.name
            it.photos.size shouldBe request.photosCount
            it.statistics.quantityView shouldBe 0
            it.parameters.size shouldBe request.parametersCount
        }
    }

    @Test
    fun `EditLot correct editing lot`() {
        // Creating lot
        val (sellerId, addressId) = generateIds(2)
        val seller = generateSellerProfile(sellerId, isChat = true)
        val saveLot = lotService.createLot(
            LotEntities.mockCreateLot(
                communicationChannel = CommunicationChannel.CHAT,
                title = "title",
                description = "description",
                price = 5
            ), seller, addressId
        )

        // Editing lot
        val request = LotEntities.mockEditLotRequest(
            communicationChannel = CommunicationChannel.VK,
            title = "title1",
            description = "description1",
            price = 6
        )
        val seller1 = generateSellerProfile(sellerId, isVk = true)
        val actualLot = lotService.editLot(saveLot, request, seller1)

        actualLot.asClue {
            it.lotFormId shouldBe saveLot.lotFormId
            it.categoryId shouldBe saveLot.categoryId
            it.title shouldBe request.title
            it.description shouldBe request.description
            it.addressId shouldBe addressId
            it.sellerId shouldBe sellerId
            it.price shouldBe request.price
            it.status shouldBe LotStatus.ACTIVE
            it.gender?.name shouldBe saveLot.gender?.name
            it.photos.size shouldBe request.photosCount
            it.parameters.size shouldBe request.parametersCount
        }
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["SOLD", "UNRECOGNIZED"], value = ClosingLotReason::class)
    fun `CloseLot closing lot because other reasons that not sold on our platform`(reason: ClosingLotReason) {
        val lot = lotRepository.save(LotEntities.mockLotEntity())

        val request = closeLotRequest {
            closingLotReason = reason
        }

        lotService.closeLot(lot, request)


        val closedLot = lotRepository.findById(lot.id).get()

        closedLot.status shouldBe LotStatus.CANCELED
    }

    @Test
    fun `CloseLot correct closing lot because of sold on our platform`() {
        val lot = lotRepository.save(LotEntities.mockLotEntity())

        val request = closeLotRequest {
            closingLotReason = ClosingLotReason.SOLD
            customerId = lot.sellerId!! + 1
        }

        lotService.closeLot(lot, request)

        val closedLot = lotRepository.findById(lot.id).get()

        closedLot.status shouldBe LotStatus.CLOSED
    }

    @Test
    fun `CloseLot should throw after closing lot because of sold on our platform without customer id`() {
        val lot = lotRepository.save(LotEntities.mockLotEntity())

        val request = closeLotRequest {
            closingLotReason = ClosingLotReason.SOLD
        }

        shouldThrow<LotException> { lotService.closeLot(lot, request) }
    }

    @Test
    fun `CloseLot should throw after closing lot because of sold on our platform with same customer id as seller id`() {
        val lot = lotRepository.save(LotEntities.mockLotEntity())

        val request = closeLotRequest {
            closingLotReason = ClosingLotReason.SOLD
            customerId = lot.sellerId!!
        }

        shouldThrow<LotException> { lotService.closeLot(lot, request) }
    }
}