package ru.zveron.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class LotForm(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0,
    val form: String,
    val type: String
)