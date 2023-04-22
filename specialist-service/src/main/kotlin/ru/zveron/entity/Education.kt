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
data class Education(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "education_id_seq")
    @SequenceGenerator(name = "education_id_seq", allocationSize = 1, initialValue = 100)
    val id: Long = -1,
    @Column(length = 100)
    val educationalInstitution: String,
    @Column(length = 100)
    val faculty: String,
    val specialization: String,
    val startYear: Int,
    val endYear: Int,
    val diplomaUrl: String,
    val showPhoto: Boolean,
    @ManyToOne
    @JoinColumn(name = "specialist_id", nullable = false, updatable = false)
    val specialist: Specialist,
)