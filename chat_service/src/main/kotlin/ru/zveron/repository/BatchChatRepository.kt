package ru.zveron.repository

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.BatchStatement
import com.datastax.oss.driver.api.core.cql.BatchType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class BatchChatRepository {

    private val updateMessagesStatusQuery = "UPDATE message SET is_read = true WHERE chat_id = :chat_id AND id = :id;"

    @Autowired
    lateinit var session: CqlSession

    suspend fun markMessagesAsRead(chatId: UUID, ids: List<UUID>) {
        val statement = session.prepare(updateMessagesStatusQuery)
        val statements = ids.map { statement.bind(chatId, it) }

        session.execute(BatchStatement.newInstance(BatchType.UNLOGGED, statements))
    }
}