package ru.zveron.entity

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity

@Entity
data class LotsFavoritesCounter(
    @EmbeddedId
    val id: LotsFavoritesCounterKey,
    val counter: Long
) {
    @Embeddable
    data class LotsFavoritesCounterKey(
        @Column(nullable = false)
        val lotId: Long,
        @Column(nullable = false)
        val shardId: Int
    ) : Serializable
}
