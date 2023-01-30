package ru.zveron.authservice

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import ru.zveron.authservice.config.ContainerConfigurer

@SpringBootTest
class AuthServiceApplicationTests : ContainerConfigurer() {
    @Test
    fun contextLoads() {
    }
}
