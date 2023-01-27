package ru.zveron.entity

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
    val street: String,
    val house: String,
    val longitude: Double,
    val latitude: Double
)
