package ru.zveron.servicemng

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(
    properties = [
        "spring.main.web-application-type=reactive"
    ]
)
class ServiceManagementServiceApplicationTests {

    @Test
    fun contextLoads() {
    }

}
