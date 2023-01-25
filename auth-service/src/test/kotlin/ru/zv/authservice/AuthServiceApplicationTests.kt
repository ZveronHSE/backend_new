package ru.zv.authservice

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import ru.zv.authservice.config.ContainerConfigurer

@SpringBootTest
class AuthServiceApplicationTests : ContainerConfigurer() {
    @Test
    fun contextLoads() {
    }
}
