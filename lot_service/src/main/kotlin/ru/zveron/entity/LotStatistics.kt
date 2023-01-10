package ru.zveron.entity

import org.hibernate.Hibernate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.MapsId
import javax.persistence.OneToOne

@Entity
data class LotStatistics(
    @Id
    val id: Long = -1,
    @Column(name = "quantity_view")
    val quantityView: Int = 0,
    @MapsId
    @OneToOne
    val lot: Lot
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as LotStatistics

        return id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return "LotStatistics(id = $id, quantity_view = $quantityView)"
    }
}