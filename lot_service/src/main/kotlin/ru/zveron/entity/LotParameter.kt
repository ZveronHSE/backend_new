package ru.zveron.entity

import org.hibernate.Hibernate
import java.io.Serializable
import java.util.Objects
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
data class LotParameter(
    @EmbeddedId
    val id: LotParameterKey,
    val value: String, // скорее всего в JSONB надо переделать?
    @ManyToOne
    @JoinColumn(name = "lot_id", insertable = false, updatable = false)
    val lot: Lot
) {

    @Embeddable
    data class LotParameterKey(
        @Column(name = "parameter_id", nullable = false)
        val parameter: Int,
        @Column(name = "lot_id", nullable = false)
        val lot: Long,
    ) : Serializable

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as LotParameter

        return id == other.id
    }

    override fun hashCode(): Int = Objects.hash(id);

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(EmbeddedId = $id , value = $value , lot = $lot )"
    }
}