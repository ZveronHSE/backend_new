package ru.zveron.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.zveron.model.entity.Connection

interface ConnectionRepository : CoroutineCrudRepository<Connection, Long>