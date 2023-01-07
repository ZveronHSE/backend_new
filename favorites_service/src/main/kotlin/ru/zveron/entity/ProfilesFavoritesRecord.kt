package ru.zveron.entity

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity

@Entity
data class ProfilesFavoritesRecord(
    @EmbeddedId
    val id : ProfilesFavoritesKey
) {
    @Embeddable
    data class ProfilesFavoritesKey(
        @Column(nullable = false)
        val ownerUserId: Long,
        @Column(nullable = false)
        val favoriteUserId: Long
    ) : Serializable
}
