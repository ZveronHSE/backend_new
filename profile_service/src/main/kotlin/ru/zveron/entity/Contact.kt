package ru.zveron.entity

import org.hibernate.Hibernate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.MapsId
import javax.persistence.OneToOne

@Entity
data class Contact(
    @Id
    @Column(nullable = false)
    var id: Long = 0,
    var vkRef: String,
    var gmail: String,
    var additionalEmail: String,
    var phone: String,
    @OneToOne
    @MapsId
    val profile: Profile,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Contact

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "${this::class.simpleName}(id=$id)"
    }
}