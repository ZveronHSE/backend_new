package ru.zveron.entity

import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.Hibernate
import org.hibernate.annotations.TypeDef
import java.time.Instant
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToOne
import javax.persistence.PrimaryKeyJoinColumn

@Entity
@TypeDef(name = "json", typeClass = JsonType::class)
data class Profile(
    @Id
    @Column(nullable = false)
    val id: Long = 0,
    @Column(nullable = false, length = 50)
    val name: String,
    @Column(nullable = false, length = 50)
    val surname: String,
    val imageId: Long,
    @Column(nullable = false)
    val lastSeen: Instant,
    val addressId: Long = -1,
) {
    @PrimaryKeyJoinColumn
    @OneToOne(mappedBy = "profile", cascade = [CascadeType.ALL])
    lateinit var contact: Contact

    @PrimaryKeyJoinColumn
    @OneToOne(mappedBy = "profile", cascade = [CascadeType.ALL])
    lateinit var settings: Settings

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        return id == (other as Profile).id
    }

    override fun hashCode(): Int = id.hashCode()

    @Override
    override fun toString(): String {
        return "${this::class.simpleName}(id = $id)"
    }
}
