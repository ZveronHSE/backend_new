package ru.zveron.review

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import ru.zveron.review.config.ContainerConfiguration

@SpringBootTest(
    properties = [
        "spring.main.web-application-type=reactive"
    ]
)
class ReviewServiceApplicationTest : ContainerConfiguration() {

    @Test
    fun contextLoads() {
    }

}
