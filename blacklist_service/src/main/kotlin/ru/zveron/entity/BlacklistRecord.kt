package ru.zveron.entity

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity

@Entity
data class BlacklistRecord(
    @EmbeddedId
    val id: BlacklistKey
) {
    @Embeddable
    data class BlacklistKey(
        @Column(nullable = false)
        val ownerUserId: Long,
        @Column(nullable = false)
        val reportedUserId: Long,
    ) : Serializable
}
