package ru.zveron.entity

import org.hibernate.Hibernate
import javax.persistence.*

@Entity
data class LotPhoto(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lot_photo_id_seq")
    @SequenceGenerator(name = "lot_photo_id_seq", allocationSize = 1, initialValue = 1)
    var id: Long = 0,
    @ManyToOne
    @JoinColumn(name = "lot_id", nullable = false, updatable = false)
    val lot: Lot,
    @JoinColumn(name = "image_id")
    var imageId: Long,
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
        return "LotPhoto(id = $id , lot = $lot , order_photo = $orderPhoto )"
    }
}