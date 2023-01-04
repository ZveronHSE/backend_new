package ru.zveron.entity

import org.hibernate.Hibernate
import javax.persistence.*

@Entity
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0,
    val name: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parent")
    var parent: Category? = null
) {
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    val categories: MutableSet<Category> = mutableSetOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Category

        return id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return "Category(id = $id , name = $name)"
    }
}