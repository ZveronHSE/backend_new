package ru.zveron.config

import com.ninjasquad.springmockk.MockkBean
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import ru.zveron.client.address.SubwayGrpcClient
import ru.zveron.client.animal.AnimalGrpcClient
import ru.zveron.client.profile.ProfileGrpcClient
import ru.zveron.persistence.repository.OrderLotRepository

@ActiveProfiles("test")
@SpringBootTest(
    properties = [
        "spring.main.web-application-type=reactive"
    ]
)
class BaseOrderApplicationTest() : ContainerConfiguration() {

    @Autowired
    lateinit var orderRepository: OrderLotRepository

    @MockkBean
    lateinit var subwayGrpcClient: ru.zveron.client.address.SubwayGrpcClient

    @MockkBean
    lateinit var profileGrpcClient: ProfileGrpcClient

    @MockkBean
    lateinit var animalGrpcClient: AnimalGrpcClient

    @BeforeEach
    fun cleanDb() {
        runBlocking {
            orderRepository.deleteAll()
        }
    }
}
