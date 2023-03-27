package ru.zveron.authservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.await
import org.springframework.test.context.ActiveProfiles
import ru.zveron.authservice.cron.SessionCronScheduler
import ru.zveron.authservice.cron.StateContextCronScheduler
import ru.zveron.authservice.grpc.client.ProfileServiceClient
import ru.zveron.authservice.webclient.notifier.NotifierClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BaseAuthTest : ContainerConfigurer() {

    @Autowired
    protected lateinit var template: R2dbcEntityTemplate

    @MockkBean
    lateinit var notifierClient: NotifierClient

    @MockkBean
    lateinit var profileClient: ProfileServiceClient

    @SpykBean
    lateinit var sessionCronScheduler: SessionCronScheduler

    @SpykBean
    lateinit var stateContextCronScheduler: StateContextCronScheduler

    protected val objectMapper: ObjectMapper = ObjectMapper().findAndRegisterModules()

    @BeforeEach
    fun cleanDb() = runBlocking {
        template.databaseClient.sql("TRUNCATE state_context, session").await()
    }
}
