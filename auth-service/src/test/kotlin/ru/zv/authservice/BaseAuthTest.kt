package ru.zv.authservice

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.await
import ru.zv.authservice.utils.ContainerConfigurer

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BaseAuthTest : ContainerConfigurer() {

    @Autowired
    protected lateinit var template: R2dbcEntityTemplate

    @BeforeEach
    fun cleanDb() = runBlocking {
        template.databaseClient.sql("TRUNCATE flow_context").await()
    }
}