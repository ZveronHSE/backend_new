package ru.zveron.service

import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import ru.zveron.DataBaseApplicationTest
import ru.zveron.repository.SubwayStationRepository
import ru.zveron.util.CreateEntitiesUtil.testGetSubwayByCityRequest
import ru.zveron.util.CreateEntitiesUtil.testSubwayStation

class ExternalSubwayStationServiceTest @Autowired constructor(
    private val template: TransactionTemplate,
    private val repository: SubwayStationRepository,
    private val service: ExternalSubwayStationService,
) : DataBaseApplicationTest() {


    @Test
    fun `given get subways by city, when subways present, then returns subways`() {
        //prep data
        val subways = List(10) { testSubwayStation().copy(city = "city") }

        //prep env
        template.executeWithoutResult { repository.saveAll(subways) }

        //when
        val response = template.execute {
            runBlocking {
                service.getSubwayStationsByCity(testGetSubwayByCityRequest(city = "city"))
            }
        }

        //then
        response.asClue {
            it.shouldNotBeNull()
            it.subwayStationsList.size shouldBe subways.size
            it.subwayStationsList.map { st -> st.name } shouldContainExactlyInAnyOrder subways.map { st -> st.name }
            it.subwayStationsList.map { st -> st.colorHex } shouldContainExactlyInAnyOrder subways.map { st -> st.colorHex }
        }
    }
}