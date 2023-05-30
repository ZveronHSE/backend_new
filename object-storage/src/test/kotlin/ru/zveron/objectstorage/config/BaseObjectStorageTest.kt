package ru.zveron.objectstorage.config

import com.ninjasquad.springmockk.MockkBean
import io.opentelemetry.api.GlobalOpenTelemetry
import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import ru.zveron.objectstorage.component.YaCloudClient

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BaseObjectStorageTest {
    @MockkBean
    lateinit var yaCloudClient: YaCloudClient


    companion object {
        @JvmStatic
        @BeforeAll
        fun setUp(): Unit {
            GlobalOpenTelemetry.resetForTest()
        }
    }
}
