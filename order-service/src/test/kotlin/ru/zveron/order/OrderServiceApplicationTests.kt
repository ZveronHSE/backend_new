package ru.zveron.order

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import ru.zveron.order.config.ContainerConfiguration

@SpringBootTest(
    properties = [
        "spring.main.web-application-type=reactive"
    ]
)
class OrderServiceApplicationTests : ContainerConfiguration() {

    @Test
    fun contextLoads() {
    }

}
