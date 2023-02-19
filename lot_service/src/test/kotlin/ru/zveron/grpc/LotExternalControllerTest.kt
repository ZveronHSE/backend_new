package ru.zveron.grpc

import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.DataBaseTest
import ru.zveron.client.address.AddressClient
import ru.zveron.client.favorite.LotFavoriteClient
import ru.zveron.client.parameter.ParameterClient
import ru.zveron.client.profile.ProfileClient
import ru.zveron.contract.lot.cardLotRequest
import ru.zveron.contract.lot.waterfallRequest
import ru.zveron.exception.LotException
import ru.zveron.grpc.configuration.AuthorizedProfileElement
import ru.zveron.service.LotService
import ru.zveron.service.LotStatisticsService
import ru.zveron.test.util.GeneratorUtils
import ru.zveron.test.util.generator.AddressGenerator
import ru.zveron.test.util.generator.ProfileGenerator
import ru.zveron.test.util.model.LotEntities
import ru.zveron.test.util.model.WaterfallEntities.mockWaterfallRequest


class LotExternalControllerTest : DataBaseTest() {

    @Autowired
    lateinit var lotExternalController: LotExternalController

    @Autowired
    lateinit var lotService: LotService

    @MockkBean
    lateinit var profileClient: ProfileClient

    @MockkBean
    lateinit var addressClient: AddressClient

    @MockkBean
    lateinit var lotFavoriteClient: LotFavoriteClient

    @MockkBean
    lateinit var parameterClient: ParameterClient

    @Autowired
    lateinit var lotStatisticsService: LotStatisticsService

    companion object {
        const val USER_ID = 10L
    }

    @Test
    fun `GetWaterfall should throw exception, if didnt get any sort for waterfall`(): Unit = runBlocking {
        val request = waterfallRequest { }

        shouldThrow<LotException> { lotExternalController.getWaterfall(request) }
    }

    @ParameterizedTest
    @ValueSource(ints = [-10, -5, 0])
    fun `GetWaterfall should throw exception, if get page size less than 0`(pageSize: Int): Unit = runBlocking {
        val request = mockWaterfallRequest(pageSize = pageSize)

        shouldThrow<LotException> { lotExternalController.getWaterfall(request) }
    }


    @Test
    fun `GetCardLot get response for correct request with user`(): Unit =
        runBlocking(AuthorizedProfileElement(USER_ID)) {
            // Creating lot
            val (sellerId, addressId) = GeneratorUtils.generateIds(2)
            val lot = lotService.createLot(
                LotEntities.mockCreateLot(),
                ProfileGenerator.generateSellerProfile(sellerId),
                addressId, LotEntities.mockInfoCategory()
            )


            coEvery {
                profileClient.getProfileWithContacts(lot.sellerId!!)
            } returns ProfileGenerator.generateSellerProfile(lot.sellerId!!, isVk = true, isChat = true)

            coEvery {
                addressClient.getAddressById(lot.addressId)
            } returns AddressGenerator.generateAddress(lot.addressId)

            coEvery {
                lotFavoriteClient.checkLotIsFavorite(lot.id, USER_ID)
            } returns true

            coEvery {
                parameterClient.getParametersById(lot.parameters.map { it.id.parameter })
            } returns mapOf(1 to "first", 2 to "second")

            val cardLot = lotExternalController.getCardLot(cardLotRequest {
                id = lot.id
            })

            cardLot.asClue {
                it.id shouldBe lot.id
                it.seller.id shouldBe sellerId
                it.contact.communicationChannelCount shouldBe 2
                it.favorite shouldBe true
                it.parametersCount shouldBe 2
            }
        }
}