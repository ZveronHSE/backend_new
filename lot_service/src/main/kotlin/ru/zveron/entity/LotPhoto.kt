package ru.zveron.entity

import org.apache.commons.lang3.RandomStringUtils.randomNumeric
import org.hibernate.Hibernate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator

@Entity
data class LotPhoto(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lot_photo_id_seq")
    @SequenceGenerator(name = "lot_photo_id_seq", allocationSize = 1, initialValue = 100)
    val id: Long = -1,

    @ManyToOne
    @JoinColumn(name = "lot_id", nullable = false, updatable = false)
    val lot: Lot,

    @Column(name = "image_url")
    val imageUrl: String,

    @Column(name = "image_id")
    val imageId: String = randomNumeric(5),

    @Column(name = "order_photo")
    val orderPhoto: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as LotPhoto

        return id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return "LotPhoto(id = $id, imageUrl=$imageUrl, order_photo = $orderPhoto )"
    }
}