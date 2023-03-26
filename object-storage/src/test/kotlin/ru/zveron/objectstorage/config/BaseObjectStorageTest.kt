package ru.zveron.objectstorage.config

import com.ninjasquad.springmockk.MockkBean
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
}
