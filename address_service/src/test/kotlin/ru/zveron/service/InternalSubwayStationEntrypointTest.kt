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
import ru.zveron.grpc.InternalSubwayStationEntrypoint
import ru.zveron.repository.SubwayStationRepository
import ru.zveron.util.CreateEntitiesUtil.testGetSubwayByIdRequest
import ru.zveron.util.CreateEntitiesUtil.testGetSubwayStationsRequest
import ru.zveron.util.CreateEntitiesUtil.testSubwayStation

class InternalSubwayStationEntrypointTest @Autowired constructor(
    private val template: TransactionTemplate,
    private val repository: SubwayStationRepository,
    private val service: InternalSubwayStationEntrypoint,
) : DataBaseApplicationTest() {

    @Test
    fun `given correct request to get station by id, when present, then returns station`() {
        //prep data
        val station = testSubwayStation()

        //prep env
        val id = template.execute { repository.save(station).id }

        //when
        val response = template.execute {
            runBlocking {
                service.getSubwayStation(testGetSubwayByIdRequest(id!!))
            }
        }

        //then
        response.shouldNotBeNull().subwayStation.asClue {
            it.shouldNotBeNull()
            it.id shouldBe station.id
            it.name shouldBe station.name
            it.colorHex shouldBe station.colorHex
            it.town shouldBe station.city
        }
    }

    @Test
    fun `given request to get subways by ids, when subways are present, then returns subways`() {
        //prep data
        val stations = List(10) { testSubwayStation() }

        //prep env
        val ids = template.execute { repository.saveAll(stations).map { it.id } }

        //when
        val response = template.execute {
            runBlocking {
                service.getSubwayStations(testGetSubwayStationsRequest(ids!!))
            }
        }

        //then
        response.shouldNotBeNull().subwayStationsList.asClue {
            it.size shouldBe stations.size
            it.map { st -> st.id } shouldContainExactlyInAnyOrder stations.map { st -> st.id }
            it.map { st -> st.name } shouldContainExactlyInAnyOrder stations.map { st -> st.name }
            it.map { st -> st.colorHex } shouldContainExactlyInAnyOrder stations.map { st -> st.colorHex }
        }
    }
}
