package ru.zveron.entity

import javax.persistence.*

@Entity
data class LotForm(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = -1,
    @Column(length = 20)
    val form: String,
    @Column(length = 20)
    val type: String
)
