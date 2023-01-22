package ru.zv.authservice.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import io.grpc.Status
import io.r2dbc.postgresql.codec.Json
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.zv.authservice.persistence.entity.FlowContextEntity
import ru.zv.authservice.persistence.model.FlowContext
import ru.zv.authservice.persistence.repository.FlowStateRepository
import ru.zv.authservice.service.AuthException
import java.util.UUID
import kotlin.reflect.KClass

@Component
class FlowStateStorage(
    private val flowStateRepository: FlowStateRepository,
    private val objectMapper: ObjectMapper,
) {

    @Transactional
    suspend fun <CTX : FlowContext> createContext(context: CTX): UUID {
        return flowStateRepository.save(
            FlowContextEntity(
                sessionId = UUID.randomUUID(),
                data = context.toJson(),
            )
        ).sessionId
    }


    //todo: maybe return entity type or just session id
    @Transactional
    suspend fun <CTX : FlowContext> updateContext(sessionId: UUID, context: CTX): CTX {
        flowStateRepository.findBySessionId(sessionId)?.let {
            return flowStateRepository.save(it.copy(data = context.toJson())).toContext(context::class)
        } ?: throw AuthException("Context not found", Status.Code.UNAUTHENTICATED)
    }

    @Transactional
    suspend fun <CTX : FlowContext> getContext(sessionId: UUID, clazz: KClass<out CTX>): CTX =
        flowStateRepository.findBySessionId(sessionId)?.toContext(clazz)
            ?: throw AuthException("Context not found", Status.Code.UNAUTHENTICATED)


    private fun FlowContext.toJson() = Json.of(objectMapper.writeValueAsString(this))

    private fun <CTX : FlowContext> FlowContextEntity.toContext(clazz: KClass<CTX>): CTX =
        objectMapper.readValue(this.toString(), clazz.java)
}
