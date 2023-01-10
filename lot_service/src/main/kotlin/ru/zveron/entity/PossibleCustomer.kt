package ru.zveron.entity

import org.hibernate.Hibernate
import java.io.Serializable
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
data class PossibleCustomer(
    @EmbeddedId
    val id: PossibleCustomerKey,
    @ManyToOne
    @JoinColumn(name = "lot_id", insertable = false, updatable = false)
    var lot: Lot,
    val dateOfConversationBeginning: Instant,
) {

    @Embeddable
    data class PossibleCustomerKey(
        @Column(name = "lot_id", updatable = false, nullable = false)
        val lot: Long,
        @Column(name = "profile_id", updatable = false, nullable = false)
        val profile: Long,
    ) : Serializable

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as PossibleCustomer

        return id == other.id
    }

    override fun hashCode(): Int = Objects.hash(id)

    @Override
    override fun toString(): String {
        return "${this::class.simpleName}(id = $id, lot = $lot)"
    }
}
