package ru.zveron.entity

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.SequenceGenerator


@Entity
data class Specialist(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "specialist_id_seq")
    @SequenceGenerator(name = "specialist_id_seq", allocationSize = 1, initialValue = 100)
    val id: Long = -1,
    @Column(length = 30)
    val name: String,
    @Column(length = 30)
    val surname: String,
    @Column(length = 30)
    val patronymic: String,
    //  // Нет оценок или "4.9 (10 оценок)"
//  string rating = 3;
//  // Количество отзывов - "нет отзывов" или "14 отзывов"
//  string quantity_review = 4;
    val imgUrl: String,
    // Описание о себе
    val description: String,
) {
    @OneToMany(mappedBy = "specialist", cascade = [CascadeType.ALL])
    lateinit var educations: List<Education>

    @OneToMany(mappedBy = "specialist", cascade = [CascadeType.ALL])
    lateinit var workExperiences: List<WorkExperience>

    @OneToMany(mappedBy = "specialist", cascade = [CascadeType.ALL])
    lateinit var achievements: List<Achievement>

    @OneToMany(mappedBy = "specialist", cascade = [CascadeType.ALL])
    lateinit var services: List<Service>

    @OneToMany(mappedBy = "specialist", cascade = [CascadeType.ALL])
    lateinit var documents: List<Document>

    @OneToMany(mappedBy = "specialist", cascade = [CascadeType.ALL])
    lateinit var otherInfo: List<OtherInfo>
}