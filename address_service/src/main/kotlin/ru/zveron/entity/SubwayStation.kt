package ru.zveron.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "subway_station")
data class SubwayStation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "name", nullable = false, length = 100)
    val name: String,

    @Column(name = "color_hex", nullable = false, length = 8)
    val colorHex: String,

    @Column(name = "city", nullable = false, length = 50)
    val city: String,
)
