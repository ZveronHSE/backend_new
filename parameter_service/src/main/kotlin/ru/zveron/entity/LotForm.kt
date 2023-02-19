package ru.zveron.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
data class LotForm(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = -1,
    @ManyToOne
    @JoinColumn(name = "id_category")
    val category: Category,
    @Column(length = 20)
    val type: String
)