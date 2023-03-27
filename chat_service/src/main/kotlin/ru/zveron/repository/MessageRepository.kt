package ru.zveron.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.zveron.model.entity.Message

interface MessageRepository : CoroutineCrudRepository<Message, String>