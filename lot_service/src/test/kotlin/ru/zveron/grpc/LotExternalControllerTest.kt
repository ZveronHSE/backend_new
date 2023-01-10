package ru.zveron.grpc

import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.DataBaseTest
import ru.zveron.contract.lot.waterfallRequest
import ru.zveron.exception.LotException
import ru.zveron.test.util.model.WaterfallEntities.mockWaterfallRequest


class LotExternalControllerTest : DataBaseTest() {

    @Autowired
    lateinit var lotExternalController: LotExternalController

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
    fun `GetWaterfall get response for correct request`(): Unit = runBlocking {

    }
}