package ru.zv.authservice.persistence.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version

@Table(name = "flow_state")
data class FlowState(
    @Id
    val id: Long,
    val sessionId: String,
    val state: State,
    @CreatedDate
    val createdAt: Instant = Instant.now(),
    @LastModifiedDate
    val modifiedAt: Instant = Instant.now(),
    @Version
    val version: Long = 0,
)

enum class State {
    LOGIN,
    REGISTRATION,
}
