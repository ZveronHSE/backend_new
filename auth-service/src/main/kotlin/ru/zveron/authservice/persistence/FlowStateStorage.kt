package ru.zveron.authservice.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import io.r2dbc.postgresql.codec.Json
import mu.KLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import ru.zveron.authservice.exception.ContextExpiredException
import ru.zveron.authservice.persistence.entity.StateContextEntity
import ru.zveron.authservice.persistence.model.StateContext
import ru.zveron.authservice.persistence.repository.StateRepository
import java.time.Instant
import java.util.UUID
import kotlin.reflect.KClass

@Component
@EnableConfigurationProperties(FlowStateConfigurationProperties::class)
class FlowStateStorage(
    private val stateRepository: StateRepository,
    private val properties: FlowStateConfigurationProperties,
) {
    companion object : KLogging() {
        private val objectMapper: ObjectMapper = ObjectMapper().findAndRegisterModules()
    }

    suspend fun <CTX : StateContext> createContext(context: CTX): UUID {
        return stateRepository.save(
            StateContextEntity(
                sessionId = UUID.randomUUID(),
                data = context.toJson(),
                expiresAt = Instant.now().plusMillis(properties.expirationMs)
            )
        ).sessionId
    }

    /**
     * throws [ContextExpiredException]
     */
    suspend fun <CTX : StateContext> updateContext(sessionId: UUID, context: CTX): CTX {
        stateRepository.findBySessionId(sessionId)?.let {
            return stateRepository.save(it.copy(data = context.toJson())).data.toContext(context::class)
        } ?: throw ContextExpiredException()
    }

    /**
     * throws [ContextExpiredException]
     *
     */
    suspend fun <CTX : StateContext> getContext(sessionId: UUID, clazz: KClass<out CTX>): CTX =
        stateRepository.findBySessionId(sessionId)?.data?.toContext(clazz)
            ?: throw ContextExpiredException()

    suspend fun deleteExpired() {
        try {
            val now = Instant.now()
            val deletedRows = stateRepository.deleteAllByExpiresAtBefore(now)
            logger.info { "Deleted $deletedRows expired flow state contexts" }
        } catch (ex: Exception) {
            logger.error(ex) { "Failed to delete expired flow state contexts" }
        }
    }

    /**
     * throws [ContextExpiredException]
     */
    final suspend inline fun <reified CTX : StateContext> getContext(sessionId: UUID): CTX =
        getContext(sessionId, CTX::class)

    private fun StateContext.toJson() = Json.of(objectMapper.writeValueAsBytes(this))

    private fun <CTX : StateContext> Json.toContext(clazz: KClass<CTX>): CTX =
        objectMapper.readValue(this.asString(), clazz.java)
}

@ConstructorBinding
@ConfigurationProperties("zveron.flow-state")
data class FlowStateConfigurationProperties(
    val expirationMs: Long,
)
