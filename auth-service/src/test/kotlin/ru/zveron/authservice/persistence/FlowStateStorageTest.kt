package ru.zveron.authservice.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.await
import ru.zveron.authservice.config.ContainerConfigurer
import ru.zveron.authservice.persistence.entity.StateContextEntity
import ru.zveron.authservice.persistence.model.MobilePhoneLoginStateContext
import ru.zveron.authservice.util.randomLoginFlowContext

@Import(FlowStateStorage::class)
@DataR2dbcTest
class FlowStateStorageTest : ContainerConfigurer() {

    @Autowired
    lateinit var flowStateStorage: FlowStateStorage

    @Autowired
    lateinit var template: R2dbcEntityTemplate

    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @BeforeEach
    fun cleanDb() = runBlocking {
        template.databaseClient.sql("TRUNCATE state_context").await()
    }

    @Test
    fun `when creating new login context, then returns uuid and saves correct entity`() = runBlocking {
        val ctx = randomLoginFlowContext()
        val uuid = flowStateStorage.createContext(ctx)

        val ctxEntity = template.select(StateContextEntity::class.java).all().awaitSingle()
        ctxEntity.shouldNotBeNull()

        assertSoftly {
            ctxEntity.sessionId shouldBe uuid
            ctxEntity.version shouldBe 1
        }

        val createdCtx =
            ctxEntity.data.asString()
                .let { objectMapper.readValue(it, MobilePhoneLoginStateContext::class.java) }

        assertSoftly {
            createdCtx.deviceFp shouldBe ctx.deviceFp
            createdCtx.phoneNumber shouldBe ctx.phoneNumber
            createdCtx.isVerified shouldBe false
            createdCtx.type shouldBe ctx.type
        }
    }

    @Test
    fun `when getting created ctx, then returns correct entity`() = runBlocking {
        val ctx = randomLoginFlowContext()
        val uuid = flowStateStorage.createContext(ctx)

        val ctxEntity = flowStateStorage.getContext<MobilePhoneLoginStateContext>(uuid)
        ctxEntity.shouldNotBeNull()

        assertSoftly {
            ctxEntity.isVerified shouldBe ctx.isVerified
            ctxEntity.code shouldBe ctx.code
            ctxEntity.codeAttempts shouldBe ctx.codeAttempts
            ctxEntity.deviceFp shouldBe ctx.deviceFp
        }
    }

    @Test
    fun `when updating ctx, then returns correct entity`() = runBlocking {
        val ctx = randomLoginFlowContext()
        val uuid = flowStateStorage.createContext(ctx)

        val updatedCtx = ctx.copy(
            isVerified = true
        )

        val ctxEntity = flowStateStorage.updateContext(uuid, updatedCtx)

        assertSoftly {
            ctxEntity.isVerified shouldBe updatedCtx.isVerified
            updatedCtx.isVerified shouldNotBe ctx.isVerified
        }
    }

}
