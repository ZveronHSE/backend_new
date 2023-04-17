package ru.zveron.entity

import com.vladmihalcea.hibernate.type.array.StringArrayType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator


@TypeDefs(TypeDef(name = "string-array", typeClass = StringArrayType::class))
@Entity
data class Animal(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "animal_id_seq")
    @SequenceGenerator(name = "animal_id_seq", allocationSize = 1, initialValue = 1)
    val id: Long = 0,

    @Column(nullable = false, length = 50)
    val name: String,

    @Column(nullable = false, length = 50)
    val breed: String,

    @Column(nullable = false, length = 50)
    val species: String,

    @Column(nullable = false)
    val age: Int,

    @Type(type = "string-array")
    @Column(
        name = "image_urls",
        columnDefinition = "text[]"
    )
    val imageUrls: Array<String>,

    @Type(type = "string-array")
    @Column(
        name = "document_urls",
        columnDefinition = "text[]"
    )
    val documentUrls: Array<String>,

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    val profile: Profile,
)
