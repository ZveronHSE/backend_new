package ru.zveron.entity

import org.hibernate.Hibernate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = -1,
    @Column(length = 100)
    val name: String,
    @Column(name = "image_url")
    val imageUrl: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parent")
    var parent: Category? = null
) {
    @OneToMany(mappedBy = "parent")
    val subCategories: List<Category> = listOf()

    @OneToMany(mappedBy = "category")
    val lotForms: List<LotForm> = listOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Category

        return id == other.id && name == other.name
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return "Category(id = $id , name = $name)"
    }

}