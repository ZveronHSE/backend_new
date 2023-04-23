package ru.zveron.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.cassandra.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.zveron.model.entity.Connection

interface ConnectionRepository : CoroutineCrudRepository<Connection, Long> {

    @Query("SELECT * FROM connection WHERE profile_id = ?0 AND is_closed = false ALLOW FILTERING")
    fun findAllOpenConnectionsByProfile(profileId: Long): Flow<Connection>
}