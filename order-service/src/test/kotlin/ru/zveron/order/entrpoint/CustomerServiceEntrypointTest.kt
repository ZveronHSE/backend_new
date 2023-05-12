package ru.zveron.order.entrpoint

import io.kotest.assertions.asClue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import ru.zveron.contract.order.external.getCustomerRequest
import ru.zveron.order.client.address.GetSubwaysApiResponse
import ru.zveron.order.client.animal.GetAnimalsApiResponse
import ru.zveron.order.client.profile.dto.GetProfileApiResponse
import ru.zveron.order.config.BaseOrderApplicationTest
import ru.zveron.order.persistence.model.constant.Status
import ru.zveron.order.test.util.*

class CustomerServiceEntrypointTest @Autowired constructor(
    private val entrypoint: CustomerServiceEntrypoint,
    private val template: R2dbcEntityTemplate,
) : BaseOrderApplicationTest() {

    @Test
    fun `smoke test for get customer`() {
        //prep data

        //request
        val customerId = randomId()
        val request = getCustomerRequest { this.profileId = customerId }

        //clients responses
        val orders = List(5) { testOrderLotEntity().copy(profileId = customerId) }
        val subways = List(5) { index -> testServiceSubwayStation().copy(id = orders[index].subwayId!!) }
        val animals = List(5) { index -> testServiceAnimal().copy(id = orders[index].animalId) }
        val profileResponse = testFindProfileResponse(id = customerId)

        //prep env
        runBlocking {
            orders.forEach { template.insert(it).awaitSingle() }
        }

        coEvery { subwayGrpcClient.getSubways(any()) } returns GetSubwaysApiResponse.Success(subways)
        coEvery { profileGrpcClient.getProfile(any()) } returns GetProfileApiResponse.Success(profileResponse)
        coEvery { animalGrpcClient.getAnimals(any()) } returns GetAnimalsApiResponse.Success(animals)


        //when
        val response = runBlocking {
            entrypoint.getCustomer(request)
        }

        //then
        response.shouldNotBeNull().asClue { response ->
            response.customer.asClue { customer ->
                customer.id shouldBe customerId
                customer.name shouldBe profileResponse.name
                customer.imageUrl shouldBe profileResponse.imageUrl
            }

            response.customer.activeOrdersCount shouldBe orders.count { order -> Status.canAcceptOrder(order.status) }
            response.customer.completedOrdersCount shouldBe orders.count { order -> order.status == Status.COMPLETED }
        }
    }
}
