package ru.zveron.authservice.cron

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.verify
import io.r2dbc.postgresql.codec.Json
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.springframework.data.r2dbc.core.awaitOneOrNull
import ru.zveron.authservice.config.BaseAuthTest
import ru.zveron.authservice.persistence.entity.SessionEntity
import ru.zveron.authservice.persistence.entity.StateContextEntity
import ru.zveron.authservice.util.randomDeviceFp
import ru.zveron.authservice.util.randomId
import ru.zveron.authservice.util.randomLoginFlowContext
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class CronSchedulerTest : BaseAuthTest() {

    companion object : KLogging()

    @Test
    fun `when session is expired, cron deletes it`() = runBlocking {
        val expiredSession =
            SessionEntity(
                fingerprint = randomDeviceFp(),
                profileId = randomId(),
                expiresAt = Instant.now().minusSeconds(60)
            )
        val nonExpiredSession =
            SessionEntity(
                fingerprint = randomDeviceFp(),
                profileId = randomId(),
                expiresAt = Instant.now().plusSeconds(60)
            )

        template.insert(expiredSession).awaitSingle()
        template.insert(nonExpiredSession).awaitSingle()

        await.atMost(Duration.of(30, ChronoUnit.SECONDS)).untilAsserted {
            //context is up and working before entity is saved, but after delete is called on empty repository
            verify(atLeast = 2) { sessionCronScheduler.deleteExpired() }
        }

        val activeSession = template.select(SessionEntity::class.java).awaitOneOrNull()
        activeSession.shouldNotBeNull()
        activeSession.profileId shouldBe nonExpiredSession.profileId
    }

    @Test
    fun `when flow context is expired, cron deletes it`() = runBlocking {
        val expiredEntity = StateContextEntity(
            sessionId = UUID.randomUUID(),
            data = Json.of(objectMapper.writeValueAsBytes(randomLoginFlowContext())),
            expiresAt = Instant.now().minusSeconds(60)
        )

        val nonExpiredEntity = StateContextEntity(
            sessionId = UUID.randomUUID(),
            data = Json.of(objectMapper.writeValueAsBytes(randomLoginFlowContext())),
            expiresAt = Instant.now().plusSeconds(60)
        )

        template.insert(expiredEntity).awaitSingle()
        template.insert(nonExpiredEntity).awaitSingle()

        await.atMost(Duration.of(30, ChronoUnit.SECONDS)).untilAsserted {
            //context is up and working before entity is saved, but after delete is called on empty repository
            verify(atLeast = 2) { stateContextCronScheduler.deleteExpired() }
        }

        val activeStateCtx = template.select(StateContextEntity::class.java).awaitOneOrNull()
        activeStateCtx.shouldNotBeNull()
        activeStateCtx.sessionId shouldBe nonExpiredEntity.sessionId
    }
}
