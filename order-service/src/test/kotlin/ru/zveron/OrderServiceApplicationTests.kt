package ru.zveron

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import ru.zveron.config.ContainerConfiguration

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
