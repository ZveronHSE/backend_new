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
data class Achievement(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "achievement_id_seq")
    @SequenceGenerator(name = "achievement_id_seq", allocationSize = 1, initialValue = 100)
    val id: Long = -1,
    @Column(length = 200)
    val title: String,
    val year: Int,
    val documentUrl: String,
    val showPhoto: Boolean,
    @ManyToOne
    @JoinColumn(name = "specialist_id", nullable = false, updatable = false)
    val specialist: Specialist,
)