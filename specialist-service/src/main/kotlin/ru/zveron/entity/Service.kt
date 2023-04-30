package ru.zveron.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator

@Entity
data class Service(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_id_seq")
    @SequenceGenerator(name = "service_id_seq", allocationSize = 1, initialValue = 100)
    val id: Long = -1,
    @Column(length = 100)
    val title: String,
    val startPrice: Int?,
    val endPrice: Int?,
    val isRemotely: Boolean,
    val atHome: Boolean,
    val isHomeVisit: Boolean,
    @ManyToOne
    @JoinColumn(name = "specialist_id", nullable = false, updatable = false)
    val specialist: Specialist,
)