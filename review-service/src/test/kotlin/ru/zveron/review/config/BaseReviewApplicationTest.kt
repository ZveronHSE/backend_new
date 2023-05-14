package ru.zveron.review.config

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(
    properties = [
        "spring.main.web-application-type=reactive"
    ]
)
class BaseReviewApplicationTest() : ContainerConfiguration() {

    @BeforeEach
    fun cleanDb() {
        runBlocking {
        }
    }
}
