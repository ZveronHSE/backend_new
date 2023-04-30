package ru.zveron.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator

@Entity
data class WorkExperience(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "work_experience_id_seq")
    @SequenceGenerator(name = "work_experience_id_seq", allocationSize = 1, initialValue = 100)
    val id: Long = -1,
    val organization: String,
    val workTitle: String,
    val startYear: Int,
    val endYear: Int?,
    val documentUrl: String,
    @ManyToOne
    @JoinColumn(name = "specialist_id", nullable = false, updatable = false)
    val specialist: Specialist,
)