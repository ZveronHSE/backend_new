package ru.zv.authservice.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import io.grpc.Status
import io.r2dbc.postgresql.codec.Json
import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.zv.authservice.exceptions.AuthException
import ru.zv.authservice.persistence.entity.StateContextEntity
import ru.zv.authservice.persistence.model.StateContext
import ru.zv.authservice.persistence.repository.StateRepository
import java.util.UUID
import kotlin.reflect.KClass
@Component
class FlowStateStorage(
    private val stateRepository: StateRepository,
) {
    companion object: KLogging() {
        private val objectMapper: ObjectMapper = ObjectMapper().findAndRegisterModules()
    }

    @Transactional
    suspend fun <CTX : StateContext> createContext(context: CTX): UUID {
        return stateRepository.save(
            StateContextEntity(
                sessionId = UUID.randomUUID(),
                data = context.toJson(),
            )
        ).sessionId
    }

    @Transactional
    suspend fun <CTX : StateContext> updateContext(sessionId: UUID, context: CTX): CTX {
        stateRepository.findBySessionId(sessionId)?.let {
            return stateRepository.save(it.copy(data = context.toJson())).data.toContext(context::class)
        } ?: throw AuthException("Context not found", Status.Code.UNAUTHENTICATED)
    }

    @Transactional
    suspend fun <CTX : StateContext> getContext(sessionId: UUID, clazz: KClass<out CTX>): CTX =
        stateRepository.findBySessionId(sessionId)?.data?.toContext(clazz)
            ?: throw AuthException("Context not found", Status.Code.UNAUTHENTICATED)


    private fun StateContext.toJson() = Json.of(objectMapper.writeValueAsBytes(this))

    private fun <CTX : StateContext> Json.toContext(clazz: KClass<CTX>): CTX =
        objectMapper.readValue(this.asString(), clazz.java)
//
//    }catch (ex: Exception){
//        logger.error { "Failed to parse json data" }
//    }
}

