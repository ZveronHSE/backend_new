package ru.zveron.authservice.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import io.r2dbc.postgresql.codec.Json
import mu.KLogging
import org.springframework.stereotype.Component
import ru.zveron.authservice.exception.ContextExpiredException
import ru.zveron.authservice.persistence.entity.StateContextEntity
import ru.zveron.authservice.persistence.model.StateContext
import ru.zveron.authservice.persistence.repository.StateRepository
import java.util.UUID
import kotlin.reflect.KClass

@Component
class FlowStateStorage(
    private val stateRepository: StateRepository,
) {
    companion object : KLogging() {
        private val objectMapper: ObjectMapper = ObjectMapper().findAndRegisterModules()
    }

    suspend fun <CTX : StateContext> createContext(context: CTX): UUID {
        return stateRepository.save(
            StateContextEntity(
                sessionId = UUID.randomUUID(),
                data = context.toJson(),
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

    suspend fun <CTX : StateContext> getContext(sessionId: UUID, clazz: KClass<out CTX>): CTX =
        stateRepository.findBySessionId(sessionId)?.data?.toContext(clazz)
            ?: throw ContextExpiredException()

    /**
     * throws [ContextExpiredException]
     */
    final suspend inline fun <reified CTX : StateContext> getContext(sessionId: UUID): CTX =
        getContext(sessionId, CTX::class)

    private fun StateContext.toJson() = Json.of(objectMapper.writeValueAsBytes(this))

    private fun <CTX : StateContext> Json.toContext(clazz: KClass<CTX>): CTX =
        objectMapper.readValue(this.asString(), clazz.java)
}

