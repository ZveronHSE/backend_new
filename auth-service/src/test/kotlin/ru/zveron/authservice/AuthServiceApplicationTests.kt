package ru.zveron.authservice

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import ru.zveron.authservice.config.ContainerConfigurer

@ActiveProfiles("test")
@SpringBootTest(
    properties = [
        "spring.main.web-application-type=reactive"
    ]
)
class AuthServiceApplicationTests : ContainerConfigurer() {
    @Test
    fun contextLoads() {
    }
}
