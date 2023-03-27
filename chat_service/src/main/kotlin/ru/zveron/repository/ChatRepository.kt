package ru.zveron.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.zveron.model.entity.Chat

interface ChatRepository : CoroutineCrudRepository<Chat, Long>