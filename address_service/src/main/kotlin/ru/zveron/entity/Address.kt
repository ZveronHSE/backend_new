package ru.zveron.entity

import org.hibernate.Hibernate
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["longitude", "latitude"])])
data class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_id_seq")
    @SequenceGenerator(name = "address_id_seq", allocationSize = 1, initialValue = 1)
    var id: Long = 0,
    val region: String? = null,
    val district: String? = null,
    val town: String? = null,
    val street: String? = null,
    val house: String? = null,
    val longitude: Double,
    val latitude: Double
) {
//    @OneToMany(mappedBy = "address")
//    var lot: MutableList<Lot> = mutableListOf()
//
//    @OneToMany(mappedBy = "address")
//    var profile: MutableList<Profile> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Address

        return id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return "Address(id = $id , region = $region , district = $district , town = $town , street = $street , house = $house , longitude = $longitude , latitude = $latitude)"
    }
}
