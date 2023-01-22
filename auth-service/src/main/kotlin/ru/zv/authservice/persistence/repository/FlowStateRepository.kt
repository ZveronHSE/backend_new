package ru.zv.authservice.persistence.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.zv.authservice.persistence.entity.FlowContextEntity
import java.util.UUID

interface FlowStateRepository : CoroutineCrudRepository<FlowContextEntity, Long> {

     suspend fun findBySessionId(sessionId: UUID): FlowContextEntity?
}
