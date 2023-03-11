package ru.zveron.entity

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity

@Entity
data class LotsFavoritesRecord(
    @EmbeddedId
    val id: LotsFavoritesKey,
    val categoryId: Int,
) {
    @Embeddable
    data class LotsFavoritesKey(
        @Column(nullable = false)
        val ownerUserId: Long,
        @Column(nullable = false)
        val favoriteLotId: Long
    ) : Serializable
}
