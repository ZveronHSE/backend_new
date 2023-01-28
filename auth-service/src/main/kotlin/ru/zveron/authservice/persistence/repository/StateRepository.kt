package ru.zveron.authservice.persistence.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.zveron.authservice.persistence.entity.StateContextEntity
import java.util.UUID


interface StateRepository : CoroutineCrudRepository<StateContextEntity, Long> {

    suspend fun findBySessionId(sessionId: UUID): StateContextEntity?

}
