package ru.zveron.entity

import org.hibernate.Hibernate
import java.io.Serializable
import java.util.*
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
data class ParameterFromType(
    @EmbeddedId
    val id: ParameterFromTypeKey,
    @ManyToOne
    @JoinColumn(name = "id_category", insertable = false, updatable = false)
    var category: Category,
    @ManyToOne
    @JoinColumn(name = "id_lot_form", insertable = false, updatable = false)
    val lotForm: LotForm,
    @ManyToOne
    @JoinColumn(name = "id_parameter", insertable = false, updatable = false)
    val parameter: Parameter
) {

    @Embeddable
    data class ParameterFromTypeKey(
        @Column(name = "id_category", updatable = false)
        val category: Int,
        @Column(name = "id_lot_form", updatable = false)
        val lotForm: Int,
        @Column(name = "id_parameter", updatable = false)
        val parameter: Int
    ) : Serializable

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as ParameterFromType

        return id == other.id
    }

    override fun hashCode(): Int = Objects.hash(id)

    @Override
    override fun toString(): String {
        return "ParameterFromType(ID = $id)"
    }
}